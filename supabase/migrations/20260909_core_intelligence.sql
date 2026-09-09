-- ================================================================================
-- FINAL DESTINY CORE INTELLIGENCE - SUPABASE DATABASE MIGRATION
-- Migration: 20260909_core_intelligence.sql
-- ================================================================================

-- 1. Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 2. User Interests Table (Dynamic Normalized Interest Vectors)
CREATE TABLE IF NOT EXISTS public.user_interests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id TEXT NOT NULL,
    topic TEXT NOT NULL,
    category_type TEXT NOT NULL DEFAULT 'GENERAL', -- CATEGORY, HASHTAG, AUDIO, CREATOR
    affinity_score FLOAT NOT NULL DEFAULT 0.5,
    interaction_count INT NOT NULL DEFAULT 1,
    last_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_topic UNIQUE (user_id, topic, category_type)
);

-- 3. User Event Telemetry Table (Explicit & Implicit Low-Level Behavioral Signals)
CREATE TABLE IF NOT EXISTS public.user_event_telemetry (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id TEXT NOT NULL,
    post_id TEXT NOT NULL,
    event_type TEXT NOT NULL, -- LIKE, COMMENT, SHARE, SAVE, DWELL, WATCH_COMPLETE, FAST_SKIP, NOT_INTERESTED
    dwell_time_ms BIGINT DEFAULT 0,
    scroll_speed FLOAT DEFAULT 0.0,
    completion_rate FLOAT DEFAULT 0.0,
    drop_off_timestamp FLOAT DEFAULT 0.0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Content Metadata Table (Rich Content Ingestion Metadata)
CREATE TABLE IF NOT EXISTS public.content_metadata (
    post_id TEXT PRIMARY KEY,
    author_id TEXT NOT NULL,
    primary_category TEXT NOT NULL DEFAULT 'General',
    sub_categories TEXT[] DEFAULT '{}',
    hashtags TEXT[] DEFAULT '{}',
    keywords TEXT[] DEFAULT '{}',
    language TEXT NOT NULL DEFAULT 'en',
    region TEXT NOT NULL DEFAULT 'Global',
    audio_id TEXT,
    quality_score FLOAT NOT NULL DEFAULT 0.8,
    viral_velocity FLOAT NOT NULL DEFAULT 0.0,
    phash TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. Creator Affinities & Social Graph Table
CREATE TABLE IF NOT EXISTS public.creator_affinities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id TEXT NOT NULL,
    creator_id TEXT NOT NULL,
    affinity_weight FLOAT NOT NULL DEFAULT 0.5,
    intimacy_score FLOAT NOT NULL DEFAULT 0.0, -- Calculated from DMs, Profile visits, Mutual likes
    last_interaction_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_creator UNIQUE (user_id, creator_id)
);

-- ================================================================================
-- INDEXES & PERFORMANCE OPTIMIZATIONS
-- ================================================================================
CREATE INDEX IF NOT EXISTS idx_user_interests_user ON public.user_interests(user_id);
CREATE INDEX IF NOT EXISTS idx_user_telemetry_user_post ON public.user_event_telemetry(user_id, post_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_created_at ON public.user_event_telemetry(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_content_metadata_category ON public.content_metadata(primary_category);
CREATE INDEX IF NOT EXISTS idx_content_metadata_trgm_hashtags ON public.content_metadata USING gin(hashtags);
CREATE INDEX IF NOT EXISTS idx_content_metadata_trgm_keywords ON public.content_metadata USING gin(keywords);

-- ================================================================================
-- HELPER FUNCTIONS & PROCEDURES
-- ================================================================================

-- Exponential Time-Decay Function (7-Day Half Life)
CREATE OR REPLACE FUNCTION apply_affinity_time_decay(p_user_id TEXT, p_half_life_days FLOAT DEFAULT 7.0)
RETURNS VOID AS $$
BEGIN
    UPDATE public.user_interests
    SET affinity_score = affinity_score * POWER(0.5, (EXTRACT(EPOCH FROM (NOW() - last_updated_at)) / (p_half_life_days * 86400))),
        last_updated_at = NOW()
    WHERE user_id = p_user_id
      AND last_updated_at < (NOW() - INTERVAL '1 day');
END;
$$ LANGUAGE plpgsql;

-- Exponential Moving Average (EMA) Viral Velocity Calculator
CREATE OR REPLACE FUNCTION update_viral_velocity(p_post_id TEXT)
RETURNS FLOAT AS $$
DECLARE
    v_recent_interactions INT;
    v_new_velocity FLOAT;
BEGIN
    -- Count interactions in last 1 hour
    SELECT COUNT(*) INTO v_recent_interactions
    FROM public.user_event_telemetry
    WHERE post_id = p_post_id
      AND created_at >= (NOW() - INTERVAL '1 hour');

    v_new_velocity := v_recent_interactions * 1.5;

    UPDATE public.content_metadata
    SET viral_velocity = (0.3 * v_new_velocity) + (0.7 * viral_velocity)
    WHERE post_id = p_post_id;

    RETURN v_new_velocity;
END;
$$ LANGUAGE plpgsql;

-- Trigram Lexical Search Reranker Function
CREATE OR REPLACE FUNCTION search_content_trigram(p_query TEXT, p_limit INT DEFAULT 20)
RETURNS TABLE (
    post_id TEXT,
    primary_category TEXT,
    similarity_score REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        cm.post_id,
        cm.primary_category,
        similarity(ARRAY_TO_STRING(cm.keywords, ' '), p_query) AS similarity_score
    FROM public.content_metadata cm
    WHERE ARRAY_TO_STRING(cm.keywords, ' ') % p_query
       OR ARRAY_TO_STRING(cm.hashtags, ' ') % p_query
    ORDER BY similarity_score DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- ================================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ================================================================================
ALTER TABLE public.user_interests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_event_telemetry ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.content_metadata ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.creator_affinities ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users access own interests" ON public.user_interests FOR ALL USING (auth.uid()::text = user_id OR user_id LIKE 'usr_%');
CREATE POLICY "Users access own telemetry" ON public.user_event_telemetry FOR ALL USING (auth.uid()::text = user_id OR user_id LIKE 'usr_%');
CREATE POLICY "Public metadata read access" ON public.content_metadata FOR SELECT USING (true);
CREATE POLICY "Users access creator affinities" ON public.creator_affinities FOR ALL USING (auth.uid()::text = user_id OR user_id LIKE 'usr_%');

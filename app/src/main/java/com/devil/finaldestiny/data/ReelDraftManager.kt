package com.devil.finaldestiny.data

import android.content.Context

data class ReelDraft(
    val mediaUri: String?,
    val caption: String,
    val filterIndex: Int,
    val location: String,
    val audioTitle: String?,
    val audioArtist: String?,
    val audioUrl: String?,
    val isReel: Boolean
)

object ReelDraftManager {
    private const val PREF_NAME = "reel_draft_prefs"
    private const val KEY_MEDIA_URI = "draft_media_uri"
    private const val KEY_CAPTION = "draft_caption"
    private const val KEY_FILTER_INDEX = "draft_filter_index"
    private const val KEY_LOCATION = "draft_location"
    private const val KEY_AUDIO_TITLE = "draft_audio_title"
    private const val KEY_AUDIO_ARTIST = "draft_audio_artist"
    private const val KEY_AUDIO_URL = "draft_audio_url"
    private const val KEY_IS_REEL = "draft_is_reel"
    private const val KEY_HAS_DRAFT = "has_draft"

    fun saveDraft(context: Context, draft: ReelDraft) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_MEDIA_URI, draft.mediaUri)
            .putString(KEY_CAPTION, draft.caption)
            .putInt(KEY_FILTER_INDEX, draft.filterIndex)
            .putString(KEY_LOCATION, draft.location)
            .putString(KEY_AUDIO_TITLE, draft.audioTitle)
            .putString(KEY_AUDIO_ARTIST, draft.audioArtist)
            .putString(KEY_AUDIO_URL, draft.audioUrl)
            .putBoolean(KEY_IS_REEL, draft.isReel)
            .putBoolean(KEY_HAS_DRAFT, true)
            .apply()
    }

    fun hasDraft(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val has = prefs.getBoolean(KEY_HAS_DRAFT, false)
        val uri = prefs.getString(KEY_MEDIA_URI, null)
        return has && !uri.isNullOrBlank()
    }

    fun getDraft(context: Context): ReelDraft? {
        if (!hasDraft(context)) return null
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val mediaUri = prefs.getString(KEY_MEDIA_URI, null) ?: return null
        return ReelDraft(
            mediaUri = mediaUri,
            caption = prefs.getString(KEY_CAPTION, "") ?: "",
            filterIndex = prefs.getInt(KEY_FILTER_INDEX, 0),
            location = prefs.getString(KEY_LOCATION, "") ?: "",
            audioTitle = prefs.getString(KEY_AUDIO_TITLE, null),
            audioArtist = prefs.getString(KEY_AUDIO_ARTIST, null),
            audioUrl = prefs.getString(KEY_AUDIO_URL, null),
            isReel = prefs.getBoolean(KEY_IS_REEL, true)
        )
    }

    fun clearDraft(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

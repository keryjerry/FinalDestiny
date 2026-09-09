package com.devil.finaldestiny.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val displayName: String, val description: String) {
    DAY_WARM("Day Warm", "Warm dull off-white (#F7F5F0) background - gentle on eyes"),
    DARK_MODE("Dark Mode", "Deep slate / charcoal (#0F172A) with muted accents"),
    NIGHT_MODE("Night Mode", "Warm amber/sepia eye-protection tint (#1F1914) for night-time reading")
}

data class ThemePalette(
    val bgLight: Color,
    val headerBg: Color,
    val cardBg: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryAccent: Color
)

object ThemeManager {
    private const val PREFS_NAME = "destiny_theme_prefs"
    private const val KEY_THEME_MODE = "destiny_theme_mode"

    private val _currentMode = MutableStateFlow(AppThemeMode.DAY_WARM)
    val currentMode: StateFlow<AppThemeMode> = _currentMode.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.DAY_WARM.name) ?: AppThemeMode.DAY_WARM.name
        _currentMode.value = try { AppThemeMode.valueOf(modeStr) } catch (e: Exception) { AppThemeMode.DAY_WARM }
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        _currentMode.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun getPalette(mode: AppThemeMode): ThemePalette {
        return when (mode) {
            AppThemeMode.DAY_WARM -> ThemePalette(
                bgLight = Color(0xFFF7F5F0),       // Warm eggshell / dull cream-white
                headerBg = Color(0xFFEFECE6),      // Soft warm header fill
                cardBg = Color(0xFFF0EEE8),        // Warm dull off-white card surface (#FAF8F5 / #F0EEE8)
                border = Color(0xFFE5E0D8),        // Soft warm border
                textPrimary = Color(0xFF1C1917),   // Warm deep charcoal
                textSecondary = Color(0xFF78716C), // Muted stone gray
                textMuted = Color(0xFFA8A29E),
                primaryAccent = Color(0xFF0284C7)
            )
            AppThemeMode.DARK_MODE -> ThemePalette(
                bgLight = Color(0xFF0F172A),       // Deep slate charcoal
                headerBg = Color(0xFF1E293B),
                cardBg = Color(0xFF1E293B),
                border = Color(0xFF334155),
                textPrimary = Color(0xFFF8FAFC),
                textSecondary = Color(0xFF94A3B8),
                textMuted = Color(0xFF64748B),
                primaryAccent = Color(0xFF38BDF8)
            )
            AppThemeMode.NIGHT_MODE -> ThemePalette(
                bgLight = Color(0xFF1F1914),       // Warm amber / sepia night tint
                headerBg = Color(0xFF2A221B),
                cardBg = Color(0xFF2A221B),
                border = Color(0xFF3D3228),
                textPrimary = Color(0xFFFDE68A),   // Warm soft amber text
                textSecondary = Color(0xFFD97706), // Warm muted amber gray
                textMuted = Color(0xFFB45309),
                primaryAccent = Color(0xFFF59E0B)
            )
        }
    }
}

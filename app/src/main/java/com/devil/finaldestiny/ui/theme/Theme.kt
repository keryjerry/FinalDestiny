package com.devil.finaldestiny.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val SkyBlueColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    secondary = BrightCyanAccent,
    tertiary = SkyBlueBorder,
    background = SkyBlueBgLight,
    surface = SkyBlueCardBg,
    onPrimary = SkyBlueCardBg,
    onSecondary = NavyTextPrimary,
    onTertiary = NavyTextPrimary,
    onBackground = NavyTextPrimary,
    onSurface = NavyTextPrimary
)

@Composable
fun FinalDestinyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = SkyBlueColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SkyBlueBgLight.toArgb()
            window.navigationBarColor = SkyBlueBgLight.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
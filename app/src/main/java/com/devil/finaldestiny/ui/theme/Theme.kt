package com.devil.finaldestiny.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = MetallicGold,
    secondary = WineRedMedium,
    tertiary = CrimsonVelvet,
    background = WineRedDark,
    surface = CardBackground,
    onPrimary = WineRedDark,
    onSecondary = LightGold,
    onTertiary = LightGold,
    onBackground = LightGold,
    onSurface = LightGold
)

@Composable
fun FinalDestinyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WineRedDark.toArgb()
            window.navigationBarColor = WineRedDark.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
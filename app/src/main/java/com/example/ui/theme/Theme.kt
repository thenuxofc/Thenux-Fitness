package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = iOSPrimaryDark,
    secondary = iOSSecondaryDark,
    tertiary = iOSTertiaryDark,
    background = iOSBackgroundDark,
    surface = iOSSurfaceDark,
    onBackground = iOSOnSurfaceDark,
    onSurface = iOSOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = iOSPrimaryLight,
    secondary = iOSSecondaryLight,
    tertiary = iOSTertiaryLight,
    background = iOSBackgroundLight,
    surface = iOSSurfaceLight,
    onBackground = iOSOnSurfaceLight,
    onSurface = iOSOnSurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

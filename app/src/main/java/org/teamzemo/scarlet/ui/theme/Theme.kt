package org.teamzemo.scarlet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val SilkColorScheme = lightColorScheme(
    primary = SilkPrimary,
    onPrimary = SilkPrimaryContent,
    primaryContainer = SilkBase200,
    onPrimaryContainer = SilkBaseContent,
    secondary = SilkPrimary,
    onSecondary = SilkPrimaryContent,
    background = SilkBase100,
    onBackground = SilkBaseContent,
    surface = SilkBase200,
    onSurface = SilkBaseContent,
    surfaceVariant = SilkBase300,
    onSurfaceVariant = SilkBaseContent,
    error = SilkError,
    onError = Color.White
)

@Composable
fun ScarletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Force light-only Silk theme to match web version
    val colorScheme = SilkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
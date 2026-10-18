package com.example.scripteretradutor.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE1BFA5),
    secondary = Color(0xFFC8A892),
    tertiary = Color(0xFFE59A86),
    background = Color(0xFF211E1B),
    surface = Color(0xFF2A2622),
    onBackground = Color(0xFFF4E9DD),
    onSurface = Color(0xFFF4E9DD)
)

private val LightColorScheme = lightColorScheme(
    primary = NotebookAccent,
    secondary = Color(0xFF9A7B68),
    tertiary = Color(0xFFB66D5C),
    background = Color(0xFFF4EFE7),
    surface = NotebookPaper,
    onBackground = NotebookInk,
    onSurface = NotebookInk
)

@Composable
fun ScripterETradutorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

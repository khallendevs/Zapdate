package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = MinimalistDarkPrimary,
    secondary = MinimalistDarkSecondary,
    tertiary = MinimalistDarkTertiary,
    background = MinimalistDarkBackground,
    surface = MinimalistDarkSurface,
    onPrimary = Color(0xFF21005D),
    onSecondary = Color(0xFF332D41),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = MinimalistDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),
    primaryContainer = MinimalistDarkPrimaryContainer,
    onPrimaryContainer = MinimalistDarkOnPrimaryContainer
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MinimalistPrimary,
    secondary = MinimalistSecondary,
    tertiary = MinimalistTertiary,
    background = MinimalistBackground,
    surface = MinimalistSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = MinimalistSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),
    primaryContainer = MinimalistPrimaryContainer,
    onPrimaryContainer = MinimalistOnPrimaryContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to maintain consistent emerald branding, but allow toggling
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

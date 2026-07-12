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

private val LightColorScheme =
  lightColorScheme(
    primary = NavyPrimary,
    secondary = GoldAccent,
    tertiary = SuccessGreen,
    background = OffWhiteBg,
    surface = CardBg,
    onPrimary = CardBg,
    onSecondary = NavyPrimary,
    onBackground = DarkText,
    onSurface = DarkText,
    error = ErrorRed
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = NavyPrimary,
    secondary = GoldAccent,
    tertiary = SuccessGreen,
    background = NavyPrimary,
    surface = DarkText,
    onPrimary = CardBg,
    onSecondary = NavyPrimary,
    onBackground = OffWhiteBg,
    onSurface = OffWhiteBg,
    error = ErrorRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set to false to enforce the strict navy and gold brand identity
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

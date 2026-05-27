package com.example.capsulecardcamera.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CapsuleRed,
    onPrimary = CapsuleWarmWhite,
    primaryContainer = CapsuleRedDark,
    onPrimaryContainer = CapsuleWarmWhite,
    secondary = CapsuleGreen,
    onSecondary = CapsuleBlack,
    tertiary = CapsuleOrange,
    onTertiary = CapsuleBlack,
    background = CapsuleSurfaceDark,
    onBackground = CapsuleWarmWhite,
    surface = CapsuleBlack,
    onSurface = CapsuleWarmWhite,
    surfaceVariant = CapsuleSurfaceDark,
    onSurfaceVariant = CapsuleWarmWhite.copy(alpha = 0.72f),
    outline = CapsuleWarmWhite.copy(alpha = 0.28f),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CapsuleRed,
    onPrimary = CapsuleWarmWhite,
    primaryContainer = CapsuleWarmWhite,
    onPrimaryContainer = CapsuleBlack,
    secondary = CapsuleGreen,
    onSecondary = CapsuleBlack,
    tertiary = CapsuleOrange,
    onTertiary = CapsuleBlack,
    background = CapsuleSurfaceLight,
    onBackground = CapsuleBlack,
    surface = CapsuleSurfaceLight,
    onSurface = CapsuleBlack,
    surfaceVariant = CapsuleWarmWhite,
    onSurfaceVariant = CapsuleOnSurfaceVariant,
    outline = CapsuleBlack.copy(alpha = 0.22f),
  )

@Composable
fun CapsuleCardCameraTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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

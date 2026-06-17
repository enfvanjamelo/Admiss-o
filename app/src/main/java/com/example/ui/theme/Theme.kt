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

private val DarkColorScheme =
  darkColorScheme(
    primary = ClinicalDarkPrimary,
    onPrimary = ClinicalDarkOnPrimary,
    primaryContainer = ClinicalDarkPrimaryContainer,
    onPrimaryContainer = ClinicalDarkOnPrimaryContainer,
    secondary = ClinicalDarkSecondary,
    onSecondary = ClinicalDarkOnSecondary,
    secondaryContainer = ClinicalDarkSecondaryContainer,
    onSecondaryContainer = ClinicalDarkOnSecondaryContainer,
    tertiary = ClinicalDarkTertiary,
    onTertiary = ClinicalDarkOnTertiary,
    tertiaryContainer = ClinicalDarkTertiaryContainer,
    onTertiaryContainer = ClinicalDarkOnTertiaryContainer,
    background = ClinicalDarkBackground,
    onBackground = ClinicalDarkOnBackground,
    surface = ClinicalDarkSurface,
    onSurface = ClinicalDarkOnSurface,
    surfaceVariant = ClinicalDarkSurfaceVariant,
    onSurfaceVariant = ClinicalDarkOnSurfaceVariant,
    outline = ClinicalDarkOutline,
    outlineVariant = ClinicalDarkOutlineVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ClinicalPrimary,
    onPrimary = ClinicalOnPrimary,
    primaryContainer = ClinicalPrimaryContainer,
    onPrimaryContainer = ClinicalOnPrimaryContainer,
    secondary = ClinicalSecondary,
    onSecondary = ClinicalOnSecondary,
    secondaryContainer = ClinicalSecondaryContainer,
    onSecondaryContainer = ClinicalOnSecondaryContainer,
    tertiary = ClinicalTertiary,
    onTertiary = ClinicalOnTertiary,
    tertiaryContainer = ClinicalTertiaryContainer,
    onTertiaryContainer = ClinicalOnTertiaryContainer,
    background = ClinicalBackground,
    onBackground = ClinicalOnBackground,
    surface = ClinicalSurface,
    onSurface = ClinicalOnSurface,
    surfaceVariant = ClinicalSurfaceVariant,
    onSurfaceVariant = ClinicalOnSurfaceVariant,
    outline = ClinicalOutline,
    outlineVariant = ClinicalOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
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

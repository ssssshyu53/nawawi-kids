package com.nawawikids.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = compositionLocalOf { false }

private val StorybookLightScheme = lightColorScheme(
    primary = LightStorybookPalette.tealMain,
    onPrimary = Color.White,
    primaryContainer = LightStorybookPalette.tealSoft,
    onPrimaryContainer = LightStorybookPalette.tealDark,
    secondary = LightStorybookPalette.goldMain,
    onSecondary = LightStorybookPalette.storyInk,
    secondaryContainer = LightStorybookPalette.goldSoft,
    onSecondaryContainer = LightStorybookPalette.goldDeep,
    tertiary = LightStorybookPalette.coralMain,
    onTertiary = Color.White,
    tertiaryContainer = LightStorybookPalette.coralSoft,
    onTertiaryContainer = LightStorybookPalette.coralDeep,
    background = LightStorybookPalette.creamBg,
    onBackground = LightStorybookPalette.storyInk,
    surface = LightStorybookPalette.surfaceWhite,
    onSurface = LightStorybookPalette.storyInk,
    surfaceVariant = LightStorybookPalette.goldSoft,
    onSurfaceVariant = LightStorybookPalette.storyMuted,
    outline = LightStorybookPalette.goldBorder,
    error = LightStorybookPalette.coralDark,
    onError = Color.White
)

private val StorybookDarkScheme = darkColorScheme(
    primary = NightStudyPalette.tealMain,
    onPrimary = Color.White,
    primaryContainer = NightStudyPalette.tealSoft,
    onPrimaryContainer = NightStudyPalette.tealDark,
    secondary = NightStudyPalette.goldMain,
    onSecondary = Color(0xFF2B1D12),
    secondaryContainer = NightStudyPalette.goldSoft,
    onSecondaryContainer = NightStudyPalette.goldDeep,
    tertiary = NightStudyPalette.coralMain,
    onTertiary = Color.White,
    tertiaryContainer = NightStudyPalette.coralSoft,
    onTertiaryContainer = NightStudyPalette.coralDeep,
    background = NightStudyPalette.creamBg,
    onBackground = NightStudyPalette.storyInk,
    surface = NightStudyPalette.surfaceWhite,
    onSurface = NightStudyPalette.storyInk,
    surfaceVariant = NightStudyPalette.subtleSurface,
    onSurfaceVariant = NightStudyPalette.storyMuted,
    outline = NightStudyPalette.goldBorder,
    error = NightStudyPalette.coralMain,
    onError = Color.White
)

@Composable
fun NawawiKidsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    StorybookColors.applyTheme(darkTheme)
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) StorybookDarkScheme else StorybookLightScheme,
            typography = AppTypography,
            content = content
        )
    }
}

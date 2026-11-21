package com.nawawikids.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StorybookLightScheme = lightColorScheme(
    primary = StorybookColors.TealMain,
    onPrimary = Color.White,
    primaryContainer = StorybookColors.TealSoft,
    onPrimaryContainer = StorybookColors.TealDark,
    secondary = StorybookColors.GoldMain,
    onSecondary = StorybookColors.StoryInk,
    secondaryContainer = StorybookColors.GoldSoft,
    onSecondaryContainer = StorybookColors.GoldDeep,
    tertiary = StorybookColors.CoralMain,
    onTertiary = Color.White,
    tertiaryContainer = StorybookColors.CoralSoft,
    onTertiaryContainer = StorybookColors.CoralDeep,
    background = StorybookColors.CreamBg,
    onBackground = StorybookColors.StoryInk,
    surface = StorybookColors.SurfaceWhite,
    onSurface = StorybookColors.StoryInk,
    surfaceVariant = StorybookColors.GoldSoft,
    onSurfaceVariant = StorybookColors.StoryMuted,
    outline = StorybookColors.GoldBorder,
    error = StorybookColors.CoralDark,
    onError = Color.White
)

@Composable
fun NawawiKidsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StorybookLightScheme,
        typography = AppTypography,
        content = content
    )
}

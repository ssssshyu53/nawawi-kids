package com.nawawikids.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class StorybookPalette(
    val skyLight: Color,
    val skySoft: Color,
    val creamBg: Color,
    val creamWarm: Color,
    val surfaceWhite: Color,
    val topBarSurface: Color,
    val subtleSurface: Color,
    val subtleBorder: Color,

    val goldLight: Color,
    val goldSoft: Color,
    val goldBorder: Color,
    val goldLip: Color,
    val goldMain: Color,
    val goldDark: Color,
    val goldDeep: Color,

    val tealSoft: Color,
    val tealBorder: Color,
    val tealMain: Color,
    val tealDark: Color,

    val coralSoft: Color,
    val coralBorder: Color,
    val coralMain: Color,
    val coralDark: Color,
    val coralDeep: Color,

    val violetSoft: Color,
    val violetBorder: Color,
    val violetMain: Color,
    val violetDark: Color,
    val violetDeep: Color,

    val skyAccent: Color,
    val skyDark: Color,

    val emeraldSoft: Color,
    val emeraldBorder: Color,
    val emeraldMain: Color,
    val emeraldDark: Color,

    val storyInk: Color,
    val storyNavy: Color,
    val storyMuted: Color,
    val lockedBg: Color,
    val lockedInk: Color,
    val lockedLip: Color,

    val activeWordBg: Color,
    val activeWordInk: Color,
    val bgTop: Color,
    val bgMidTop: Color,
    val matnTop: Color,
    val matnBottom: Color,
    val matnBorder: Color,
    val matnLip: Color
)

val LightStorybookPalette = StorybookPalette(
    skyLight = Color(0xFFBAE6FD),
    skySoft = Color(0xFFE0F2FE),
    creamBg = Color(0xFFFFF9EC),
    creamWarm = Color(0xFFFDECC7),
    surfaceWhite = Color(0xFFFFFFFF),
    topBarSurface = Color(0xF5FFFFFF),
    subtleSurface = Color(0xFFF1F5F9),
    subtleBorder = Color(0xFFE2E8F0),

    goldLight = Color(0xFFFEF08A),
    goldSoft = Color(0xFFFEF3C7),
    goldBorder = Color(0xFFF0D49C),
    goldLip = Color(0xFFE6C17A),
    goldMain = Color(0xFFF59E0B),
    goldDark = Color(0xFFB45309),
    goldDeep = Color(0xFF78350F),

    tealSoft = Color(0xFFCCFBF1),
    tealBorder = Color(0xFF5EEAD4),
    tealMain = Color(0xFF14B8A6),
    tealDark = Color(0xFF0F766E),

    coralSoft = Color(0xFFFFE4E6),
    coralBorder = Color(0xFFFDA4AF),
    coralMain = Color(0xFFF43F5E),
    coralDark = Color(0xFFBE123C),
    coralDeep = Color(0xFF881337),

    violetSoft = Color(0xFFF5F3FF),
    violetBorder = Color(0xFFC4B5FD),
    violetMain = Color(0xFF8B5CF6),
    violetDark = Color(0xFF6D28D9),
    violetDeep = Color(0xFF4C1D95),

    skyAccent = Color(0xFF0EA5E9),
    skyDark = Color(0xFF0369A1),

    emeraldSoft = Color(0xFFDCFCE7),
    emeraldBorder = Color(0xFF4ADE80),
    emeraldMain = Color(0xFF22C55E),
    emeraldDark = Color(0xFF15803D),

    storyInk = Color(0xFF2B1D12),
    storyNavy = Color(0xFF1E3A5F),
    storyMuted = Color(0xFF6F5236),
    lockedBg = Color(0xFFEFE6D4),
    lockedInk = Color(0xFFA8916B),
    lockedLip = Color(0xFFCDB991),

    activeWordBg = Color(0xFFFDE047),
    activeWordInk = Color(0xFF2B1D12),
    bgTop = Color(0xFFDFF3FF),
    bgMidTop = Color(0xFFF2FAFF),
    matnTop = Color(0xFFFFFFFF),
    matnBottom = Color(0xFFFFFDF6),
    matnBorder = Color(0xFFEAB308),
    matnLip = Color(0xFFE2B857)
)

/**
 * Calm, harmonious warm-charcoal Night Study palette engineered for children's eyes.
 * Avoids harsh neon borders and glaring blue-black contrast, using warm muted charcoal
 * surfaces, soft moonlight gold accents, and warm parchment-ivory typography.
 */
val NightStudyPalette = StorybookPalette(
    skyLight = Color(0xFF283A4E),
    skySoft = Color(0xFF1A2433),
    creamBg = Color(0xFF151923),
    creamWarm = Color(0xFF1A1F2B),
    surfaceWhite = Color(0xFF1F2533),
    topBarSurface = Color(0xF7161B26),
    subtleSurface = Color(0xFF283042),
    subtleBorder = Color(0xFF364056),

    goldLight = Color(0xFFFDE68A),
    goldSoft = Color(0xFF29241B),
    goldBorder = Color(0xFF4C402B),
    goldLip = Color(0xFF11151E),
    goldMain = Color(0xFFE0A43A),
    goldDark = Color(0xFFF0C264),
    goldDeep = Color(0xFFF9E0A2),

    tealSoft = Color(0xFF19292C),
    tealBorder = Color(0xFF2B4F54),
    tealMain = Color(0xFF1F9D90),
    tealDark = Color(0xFF7AD8CE),

    coralSoft = Color(0xFF2B1E24),
    coralBorder = Color(0xFF5A3340),
    coralMain = Color(0xFFD94F6B),
    coralDark = Color(0xFFF4A0B0),
    coralDeep = Color(0xFFFBC7D1),

    violetSoft = Color(0xFF221F32),
    violetBorder = Color(0xFF423A61),
    violetMain = Color(0xFF846EE0),
    violetDark = Color(0xFFC2B4FA),
    violetDeep = Color(0xFFDDD5FE),

    skyAccent = Color(0xFF369AD4),
    skyDark = Color(0xFF88CDF2),

    emeraldSoft = Color(0xFF192B22),
    emeraldBorder = Color(0xFF2B523E),
    emeraldMain = Color(0xFF2CB06C),
    emeraldDark = Color(0xFF78DBA8),

    storyInk = Color(0xFFF0ECE3),
    storyNavy = Color(0xFFE7ECF5),
    storyMuted = Color(0xFF9AA5B8),
    lockedBg = Color(0xFF1A1F2B),
    lockedInk = Color(0xFF778296),
    lockedLip = Color(0xFF10141C),

    activeWordBg = Color(0xFF8C5313),
    activeWordInk = Color(0xFFFFFBEB),
    bgTop = Color(0xFF11151E),
    bgMidTop = Color(0xFF141822),
    matnTop = Color(0xFF202634),
    matnBottom = Color(0xFF1B212E),
    matnBorder = Color(0xFF69532B),
    matnLip = Color(0xFF10141C)
)

object StorybookColors {
    var isDarkMode by mutableStateOf(false)
        private set

    private var palette by mutableStateOf(LightStorybookPalette)

    fun applyTheme(dark: Boolean) {
        if (isDarkMode != dark) {
            isDarkMode = dark
            palette = if (dark) NightStudyPalette else LightStorybookPalette
        }
    }

    val SkyLight: Color get() = palette.skyLight
    val SkySoft: Color get() = palette.skySoft
    val CreamBg: Color get() = palette.creamBg
    val CreamWarm: Color get() = palette.creamWarm
    val SurfaceWhite: Color get() = palette.surfaceWhite
    val TopBarSurface: Color get() = palette.topBarSurface
    val SubtleSurface: Color get() = palette.subtleSurface
    val SubtleBorder: Color get() = palette.subtleBorder

    val GoldLight: Color get() = palette.goldLight
    val GoldSoft: Color get() = palette.goldSoft
    val GoldBorder: Color get() = palette.goldBorder
    val GoldLip: Color get() = palette.goldLip
    val GoldMain: Color get() = palette.goldMain
    val GoldDark: Color get() = palette.goldDark
    val GoldDeep: Color get() = palette.goldDeep

    val TealSoft: Color get() = palette.tealSoft
    val TealBorder: Color get() = palette.tealBorder
    val TealMain: Color get() = palette.tealMain
    val TealDark: Color get() = palette.tealDark

    val CoralSoft: Color get() = palette.coralSoft
    val CoralBorder: Color get() = palette.coralBorder
    val CoralMain: Color get() = palette.coralMain
    val CoralDark: Color get() = palette.coralDark
    val CoralDeep: Color get() = palette.coralDeep

    val VioletSoft: Color get() = palette.violetSoft
    val VioletBorder: Color get() = palette.violetBorder
    val VioletMain: Color get() = palette.violetMain
    val VioletDark: Color get() = palette.violetDark
    val VioletDeep: Color get() = palette.violetDeep

    val SkyAccent: Color get() = palette.skyAccent
    val SkyDark: Color get() = palette.skyDark

    val EmeraldSoft: Color get() = palette.emeraldSoft
    val EmeraldBorder: Color get() = palette.emeraldBorder
    val EmeraldMain: Color get() = palette.emeraldMain
    val EmeraldDark: Color get() = palette.emeraldDark

    val StoryInk: Color get() = palette.storyInk
    val StoryNavy: Color get() = palette.storyNavy
    val StoryMuted: Color get() = palette.storyMuted
    val LockedBg: Color get() = palette.lockedBg
    val LockedInk: Color get() = palette.lockedInk
    val LockedLip: Color get() = palette.lockedLip

    val ActiveWordBg: Color get() = palette.activeWordBg
    val ActiveWordInk: Color get() = palette.activeWordInk
    val BgTop: Color get() = palette.bgTop
    val BgMidTop: Color get() = palette.bgMidTop
    val MatnTop: Color get() = palette.matnTop
    val MatnBottom: Color get() = palette.matnBottom
    val MatnBorder: Color get() = palette.matnBorder
    val MatnLip: Color get() = palette.matnLip

    // Fixed button shade constants so CandyButton / RibbonBadge gradients always keep crisp white text contrast
    val TealButtonBottom = Color(0xFF0F766E)
    val TealButtonBorder = Color(0xFF115E59)
    val GoldButtonInk = Color(0xFF451A03)
}

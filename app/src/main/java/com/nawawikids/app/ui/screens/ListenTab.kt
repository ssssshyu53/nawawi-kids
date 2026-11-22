package com.nawawikids.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Forward5
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Replay5
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.audio.AudioPlaybackState
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.StorybookChapters
import com.nawawikids.app.model.WordTiming
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.CloudMatnCard
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.AmiriFontFamily
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListenTab(
    hadith: Hadith,
    audioState: AudioPlaybackState,
    fontSize: FontSizeOption,
    onSeekAudio: (Double, Boolean) -> Unit,
    onSelectFontSize: (FontSizeOption) -> Unit,
    onOpenPoster: () -> Unit,
    onGoToLearn: () -> Unit
) {
    val chapter = StorybookChapters.byId(hadith.stage)
    val groupedParagraphs = remember(hadith.id) {
        hadith.words
            .withIndex()
            .groupBy { it.value.p }
            .toSortedMap()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Scalloped Cloud Word-by-Word Matn Card (opens immediately without scrolling past a giant player!)
        CloudMatnCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AssetImage(
                        assetPath = "img/book_guide.png",
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "نص الحديث — اضغط أي كلمة لسماعها",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.GoldDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Font Size Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(StorybookColors.GoldSoft)
                        .border(1.5.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    FontSizeOption.entries.forEach { opt ->
                        val active = opt == fontSize
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (active) StorybookColors.TealMain else Color.Transparent)
                                .clickable { onSelectFontSize(opt) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = opt.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (active) Color.White else StorybookColors.GoldDeep
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(StorybookColors.GoldBorder)
            )
            Spacer(Modifier.height(12.dp))

            // Clean Continuous Hadith Text
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groupedParagraphs.forEach { (_, indexedWords) ->
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        indexedWords.forEach { (idx, word) ->
                            val isActive = idx == audioState.activeWordIndex
                            val isSpoken = audioState.activeWordIndex > -1 && idx < audioState.activeWordIndex
                            val bg = when {
                                isActive -> StorybookColors.ActiveWordBg
                                isSpoken -> StorybookColors.TealSoft
                                else -> Color.Transparent
                            }
                            val border = when {
                                isActive -> StorybookColors.GoldMain
                                else -> Color.Transparent
                            }
                            val textCol = when {
                                isActive -> StorybookColors.ActiveWordInk
                                isSpoken -> StorybookColors.TealDark
                                else -> StorybookColors.StoryInk
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .border(1.5.dp, border, RoundedCornerShape(10.dp))
                                    .clickable { onSeekAudio(word.s, true) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = word.t,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = fontSize.spSize.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = textCol,
                                    lineHeight = (fontSize.spSize * 1.55f).sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            CandyButton(
                onClick = onGoToLearn,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goto_learn_button"),
                topColor = if (StorybookColors.isDarkMode) Color(0xFFE6B44C) else Color(0xFFFDE047),
                bottomColor = if (StorybookColors.isDarkMode) Color(0xFFC98C22) else StorybookColors.GoldMain,
                borderColor = if (StorybookColors.isDarkMode) Color(0xFFE0A83B) else Color(0xFFD97706),
                lipColor = Color(0xFF8C5A0B),
                contentColor = Color(0xFF5C2C06)
            ) {
                Text(
                    text = "الخطوة ٢: هيا نفهم الحديث",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5C2C06)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF5C2C06),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 1b. Optional Author Commentary Card ("تعقيب من المؤلف — غير مطلوب للتسميع")
        if (hadith.authorNote.isNotEmpty()) {
            AuthorCommentaryListenCard(
                authorNoteWords = hadith.authorNote,
                audioState = audioState,
                fontSize = fontSize,
                onSeekAudio = onSeekAudio
            )
        }

        // 2. Illustrated Poster Card
        ToyCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(StorybookColors.CoralSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = StorybookColors.CoralDark,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Text(
                    text = if (!hadith.poster.isNullOrBlank()) "اللوحة المصورة للحديث" else "لوحة المرحلة المصورة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.StoryNavy
                )
            }

            Spacer(Modifier.height(10.dp))

            if (!hadith.poster.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.5.dp, StorybookColors.GoldMain, RoundedCornerShape(20.dp))
                        .clickable(onClick = onOpenPoster)
                ) {
                    AssetImage(
                        assetPath = hadith.poster,
                        contentDescription = hadith.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StorybookColors.StoryNavy.copy(alpha = 0.88f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.ZoomIn, contentDescription = null, tint = StorybookColors.GoldLight, modifier = Modifier.size(16.dp))
                            Text("اضغط لتكبير اللوحة الكاملة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            } else {
                AssetImage(
                    assetPath = chapter.sceneAsset,
                    contentDescription = chapter.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuthorCommentaryListenCard(
    authorNoteWords: List<WordTiming>,
    audioState: AudioPlaybackState,
    fontSize: FontSizeOption,
    onSeekAudio: (Double, Boolean) -> Unit
) {
    val groupedNoteParagraphs = remember(authorNoteWords) {
        authorNoteWords
            .groupBy { it.p }
            .toSortedMap()
    }
    val noteFontSp = (fontSize.spSize - 2f).coerceAtLeast(18f)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = StorybookColors.VioletSoft,
        border = BorderStroke(2.dp, StorybookColors.VioletBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("author_commentary_listen_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StorybookColors.SurfaceWhite)
                        .border(1.5.dp, StorybookColors.VioletBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoStories,
                        contentDescription = null,
                        tint = StorybookColors.VioletDark,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "تعقيب من المؤلف — الإمام النووي رحمه الله",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.VioletDark
                    )
                    Surface(
                        shape = CircleShape,
                        color = StorybookColors.SurfaceWhite,
                        border = BorderStroke(1.dp, StorybookColors.VioletBorder)
                    ) {
                        Text(
                            text = "للفائدة فقط • غير مطلوب للحفظ أو التسميع",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = StorybookColors.VioletDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = StorybookColors.VioletBorder.copy(alpha = 0.7f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                groupedNoteParagraphs.forEach { (_, words) ->
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        words.forEach { word ->
                            val isActive = (audioState.isPlaying || audioState.currentSec > 0.0) &&
                                audioState.currentSec >= word.s &&
                                audioState.currentSec <= word.e + 0.12
                            val isSpoken = audioState.currentSec > word.e + 0.12
                            val bg = when {
                                isActive -> if (StorybookColors.isDarkMode) StorybookColors.VioletMain.copy(alpha = 0.45f) else Color(0xFFDDD6FE)
                                isSpoken -> StorybookColors.VioletBorder.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                            val border = if (isActive) StorybookColors.VioletMain else Color.Transparent
                            val textCol = when {
                                isActive -> StorybookColors.VioletDeep
                                isSpoken -> StorybookColors.VioletDark
                                else -> StorybookColors.StoryInk
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(bg)
                                    .border(1.5.dp, border, RoundedCornerShape(9.dp))
                                    .clickable { onSeekAudio(word.s, true) }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = word.t,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = noteFontSp.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = textCol,
                                    lineHeight = (noteFontSp * 1.55f).sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sleek, light, Storybook-native floating audio capsule pinned above the bottom dock
 * across all tabs of the active Hadith. Starts ultra-compact (Play/Pause + live progress +
 * 1-tap Speed) and smoothly expands upward to reveal a custom seek scrubber, ±5s jumps,
 * restart, and repeat controls.
 */
@Composable
fun StickyHadithAudioBar(
    hadithTitle: String,
    audioState: AudioPlaybackState,
    activeWordText: String?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayFromStart: () -> Unit,
    onSeekAudio: (Double, Boolean) -> Unit,
    onRewind5s: () -> Unit,
    onForward5s: () -> Unit,
    onCycleSpeed: () -> Unit,
    onCycleLoop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speedShort = when {
        audioState.speed < 0.82f -> "٠.٧٥×"
        audioState.speed < 0.96f -> "٠.٩×"
        audioState.speed > 1.05f -> "١.١٥×"
        else -> "١×"
    }
    val loopShort = when (audioState.loopMode) {
        3 -> "٣×"
        999 -> "دائم"
        else -> "تكرار"
    }

    val safeDuration = audioState.durationSec.coerceAtLeast(1.0)
    val rawFraction = (audioState.currentSec / safeDuration).toFloat().coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = tween(durationMillis = 120),
        label = "audio_top_progress"
    )
    val borderColor by animateColorAsState(
        targetValue = if (audioState.isPlaying) {
            StorybookColors.TealMain.copy(alpha = 0.65f)
        } else {
            StorybookColors.GoldBorder
        },
        animationSpec = tween(220),
        label = "player_border_color"
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chevron_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = StorybookColors.SurfaceWhite,
            shadowElevation = if (audioState.isPlaying || expanded) 10.dp else 6.dp,
            border = BorderStroke(1.5.dp, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Sleek Hairline Progress Strip integrated along the top edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(StorybookColors.SubtleSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction)
                            .height(3.5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(StorybookColors.TealMain, StorybookColors.GoldMain)
                                )
                            )
                    )
                }

                // Expandable Controls Drawer (grows upward cleanly without shifting main bar)
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(180)) + expandVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        expandFrom = Alignment.Bottom
                    ),
                    exit = fadeOut(tween(140)) + shrinkVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        shrinkTowards = Alignment.Bottom
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StorybookColors.CreamBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Custom Silky Interactive Scrubber (LTR for universal timeline intuition)
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = ArabicUtils.formatClock(audioState.currentSec),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StorybookColors.TealDark
                                )
                                SleekAudioSeekBar(
                                    progress = rawFraction,
                                    onSeekFraction = { frac ->
                                        onSeekAudio(frac * safeDuration, false)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = ArabicUtils.formatClock(audioState.durationSec),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.StoryMuted
                                )
                            }
                        }

                        // 4 Balanced Tonal Action Buttons (Zero Cramping)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DrawerActionPill(
                                icon = Icons.Rounded.Replay,
                                label = "إعادة",
                                active = false,
                                onClick = onPlayFromStart,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("audio_restart_button")
                            )
                            DrawerActionPill(
                                icon = Icons.Rounded.Replay5,
                                label = "٥ث-",
                                active = false,
                                onClick = onRewind5s,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("audio_rewind_button")
                            )
                            DrawerActionPill(
                                icon = Icons.Rounded.Forward5,
                                label = "٥ث+",
                                active = false,
                                onClick = onForward5s,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("audio_forward_button")
                            )
                            DrawerActionPill(
                                icon = Icons.Rounded.Repeat,
                                label = loopShort,
                                active = audioState.loopMode != 0,
                                onClick = onCycleLoop,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("audio_loop_button")
                            )
                        }
                    }
                }

                if (expanded) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color(0xFFF1E5CE)
                    )
                }

                // Compact Main Capsule Row (Airy, Balanced, Always 1-Tap Play & Speed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Vibrant Play / Pause Button
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                brush = if (audioState.isPlaying) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFDE047), StorybookColors.GoldMain)
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(StorybookColors.TealMain, StorybookColors.TealDark)
                                    )
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (audioState.isPlaying) Color(0xFFD97706) else StorybookColors.TealDark,
                                shape = CircleShape
                            )
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("audio_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (audioState.isPlaying) "إيقاف مؤقت" else "تشغيل الحديث",
                            tint = if (audioState.isPlaying) Color(0xFF451A03) else Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    // Center Track Info (Tapping expands/collapses the scrubber drawer)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onToggleExpanded)
                            .padding(vertical = 2.dp, horizontal = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (audioState.isPlaying) {
                                MiniEqualizerBars()
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Headphones,
                                    contentDescription = null,
                                    tint = StorybookColors.TealDark,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = hadithTitle,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = StorybookColors.StoryInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "${ArabicUtils.formatClock(audioState.currentSec)} / ${ArabicUtils.formatClock(audioState.durationSec)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.StoryMuted,
                                    maxLines = 1
                                )
                            }
                            if (!activeWordText.isNullOrBlank() && audioState.isPlaying) {
                                Text(
                                    text = "• $activeWordText",
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.TealDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else if (audioState.loopMode != 0) {
                                Text(
                                    text = "• $loopShort",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.GoldDark,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // 1-Tap Speed Pill (Always accessible!)
                    val customSpeed = audioState.speed != 1.0f
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (customSpeed) StorybookColors.TealSoft else StorybookColors.GoldSoft,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (customSpeed) StorybookColors.TealMain else StorybookColors.GoldBorder
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onCycleSpeed)
                            .testTag("audio_speed_button")
                    ) {
                        Text(
                            text = speedShort,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (customSpeed) StorybookColors.TealDark else StorybookColors.GoldDeep,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            maxLines = 1
                        )
                    }

                    // Expand / Collapse Controls Button
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (expanded) StorybookColors.TealSoft else Color(0xFFF6EFE2))
                            .border(
                                width = 1.dp,
                                color = if (expanded) StorybookColors.TealBorder else StorybookColors.GoldBorder,
                                shape = CircleShape
                            )
                            .clickable(onClick = onToggleExpanded)
                            .testTag("audio_expand_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = if (expanded) "تصغير المشغّل" else "إظهار شريط التقديم والتكرار",
                            tint = if (expanded) StorybookColors.TealDark else StorybookColors.StoryMuted,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(chevronRotation)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SleekAudioSeekBar(
    progress: Float,
    onSeekFraction: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(progress) }

    val displayFraction = if (isDragging) dragFraction else progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(28.dp)
            .onSizeChanged { trackWidthPx = it.width.coerceAtLeast(1).toFloat() }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val frac = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                    onSeekFraction(frac)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragFraction = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        onSeekFraction(dragFraction)
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        dragFraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                        onSeekFraction(dragFraction)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        ) {
            val trackHeight = 6.dp.toPx()
            val trackY = (size.height - trackHeight) / 2f
            val corner = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            val thumbRadius = if (isDragging) 9.dp.toPx() else 7.5.dp.toPx()
            val usableWidth = (size.width - thumbRadius * 2f).coerceAtLeast(1f)
            val thumbX = thumbRadius + usableWidth * displayFraction

            // Track background
            drawRoundRect(
                color = Color(0xFFEAE0CC),
                topLeft = Offset(thumbRadius, trackY),
                size = Size(usableWidth, trackHeight),
                cornerRadius = corner
            )
            // Active gradient track
            val filledW = (thumbX - thumbRadius).coerceAtLeast(0f)
            if (filledW > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(StorybookColors.TealMain, StorybookColors.GoldMain)
                    ),
                    topLeft = Offset(thumbRadius, trackY),
                    size = Size(filledW, trackHeight),
                    cornerRadius = corner
                )
            }
            // Outer white ring + crisp teal thumb
            drawCircle(
                color = Color.White,
                radius = thumbRadius + 2.dp.toPx(),
                center = Offset(thumbX, size.height / 2f)
            )
            drawCircle(
                color = StorybookColors.TealDark,
                radius = thumbRadius,
                center = Offset(thumbX, size.height / 2f)
            )
        }
    }
}

@Composable
private fun DrawerActionPill(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(13.dp),
        color = if (active) StorybookColors.GoldSoft else StorybookColors.SurfaceWhite,
        border = BorderStroke(
            width = 1.dp,
            color = if (active) StorybookColors.GoldMain else StorybookColors.GoldBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) StorybookColors.GoldDark else StorybookColors.TealDark,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (active) StorybookColors.GoldDeep else StorybookColors.StoryInk,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MiniEqualizerBars() {
    val transition = rememberInfiniteTransition(label = "mini_eq")
    val h1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(340, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(490, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq3"
    )

    Row(
        modifier = Modifier.height(14.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        listOf(h1, h2, h3).forEachIndexed { idx, frac ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((14f * frac).dp)
                    .clip(CircleShape)
                    .background(if (idx == 1) StorybookColors.GoldMain else StorybookColors.TealMain)
            )
        }
    }
}

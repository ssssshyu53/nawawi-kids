package com.nawawikids.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Replay5
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.audio.AudioPlaybackState
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.StorybookChapters
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
    onTogglePlayPause: () -> Unit,
    onSeekAudio: (Double, Boolean) -> Unit,
    onRewind5s: () -> Unit,
    onCycleSpeed: () -> Unit,
    onCycleLoop: () -> Unit,
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

    val speedLabel = when {
        audioState.speed < 0.9f -> "بطيء (٠.٧٥x)"
        audioState.speed > 1.05f -> "سريع (١.١٥x)"
        else -> "عادي (١x)"
    }
    val loopLabel = when (audioState.loopMode) {
        3 -> "تكرار: ٣×"
        999 -> "تكرار: دائم"
        else -> "تكرار: لا"
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Capsule Karaoke Audio Player
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StorybookColors.TealDark, RoundedCornerShape(28.dp))
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), StorybookColors.TealMain, Color(0xFF0D9488))
                    )
                )
                .border(2.5.dp, StorybookColors.TealDark, RoundedCornerShape(28.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Big Golden Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(StorybookColors.GoldDark, CircleShape)
                            .padding(bottom = 4.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFFFDE047), StorybookColors.GoldMain)),
                                CircleShape
                            )
                            .border(3.dp, Color.White, CircleShape)
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("audio_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (audioState.isPlaying) "إيقاف مؤقت" else "تشغيل التلاوة",
                            tint = Color(0xFF5C2C06),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مشغّل القراءة والتتبع كلمةً بكلمة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "اضغط على أي كلمة في السحابة البيضاء لسماعها فوراً!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCCFBF1)
                        )
                    }
                }

                // Controls Row: Rewind 5s, Speed, Loop
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CandyButton(
                        onClick = onRewind5s,
                        modifier = Modifier.testTag("audio_rewind_button"),
                        topColor = Color.White,
                        bottomColor = StorybookColors.TealSoft,
                        borderColor = Color(0xFF99F6E4),
                        lipColor = StorybookColors.TealDark,
                        contentColor = StorybookColors.TealDark,
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Rounded.Replay5, contentDescription = null, tint = StorybookColors.TealDark, modifier = Modifier.size(16.dp))
                        Text("ارجع ٥ث", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.TealDark)
                    }

                    CandyButton(
                        onClick = onCycleSpeed,
                        modifier = Modifier.testTag("audio_speed_button"),
                        topColor = StorybookColors.GoldLight,
                        bottomColor = Color(0xFFFDE047),
                        borderColor = Color(0xFFFACC15),
                        lipColor = StorybookColors.GoldDark,
                        contentColor = StorybookColors.GoldDeep,
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Rounded.Speed, contentDescription = null, tint = StorybookColors.GoldDeep, modifier = Modifier.size(16.dp))
                        Text(speedLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.GoldDeep)
                    }

                    CandyButton(
                        onClick = onCycleLoop,
                        modifier = Modifier.testTag("audio_loop_button"),
                        topColor = Color.White,
                        bottomColor = StorybookColors.SkySoft,
                        borderColor = StorybookColors.SkyLight,
                        lipColor = StorybookColors.SkyDark,
                        contentColor = StorybookColors.SkyDark,
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Rounded.Repeat, contentDescription = null, tint = StorybookColors.SkyDark, modifier = Modifier.size(16.dp))
                        Text(loopLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.SkyDark)
                    }
                }

                // Scrubber Bar (LTR for timeline)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    val maxDur = audioState.durationSec.coerceAtLeast(1.0).toFloat()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = ArabicUtils.formatClock(audioState.currentSec),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.GoldLight
                        )
                        Slider(
                            value = audioState.currentSec.toFloat().coerceIn(0f, maxDur),
                            onValueChange = { onSeekAudio(it.toDouble(), false) },
                            valueRange = 0f..maxDur,
                            colors = SliderDefaults.colors(
                                thumbColor = StorybookColors.GoldMain,
                                activeTrackColor = StorybookColors.GoldLight,
                                inactiveTrackColor = Color.White.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = ArabicUtils.formatClock(audioState.durationSec),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. Scalloped Cloud Word-by-Word Matn Card
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
                            .size(34.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "سحابة الحديث — اضغط أي كلمة لتسمعها",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.GoldDark
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
                    .height(2.dp)
                    .background(Color(0xFFFDE68A))
            )
            Spacer(Modifier.height(14.dp))

            // Interactive Karaoke Words grouped by paragraph
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                isActive -> Color(0xFFFDE047)
                                isSpoken -> StorybookColors.TealSoft
                                else -> Color.Transparent
                            }
                            val border = when {
                                isActive -> Color(0xFFD97706)
                                else -> Color.Transparent
                            }
                            val textCol = when {
                                isActive -> Color(0xFF451A03)
                                isSpoken -> StorybookColors.TealDark
                                else -> Color(0xFF23180F)
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
                topColor = Color(0xFFFDE047),
                bottomColor = StorybookColors.GoldMain,
                borderColor = Color(0xFFD97706),
                lipColor = StorybookColors.GoldDark,
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

        // 3. Illustrated Poster / Stage Scene Companion Card
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
                        .clip(RoundedCornerShape(18.dp))
                        .border(3.dp, StorybookColors.GoldMain, RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenPoster)
                ) {
                    AssetImage(
                        assetPath = hadith.poster,
                        contentDescription = hadith.title,
                        contentScale = ContentScale.FillWidth,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.5.dp, chapter.border, RoundedCornerShape(18.dp))
                        .background(chapter.bg)
                ) {
                    AssetImage(
                        assetPath = chapter.sceneAsset,
                        contentDescription = chapter.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    Text(
                        text = "${chapter.ord}: ${chapter.name}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = chapter.ink,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0F9FF),
                border = BorderStroke(2.dp, Color(0xFFBAE6FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "الفكرة الجميلة للحديث:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.SkyDark
                    )
                    Text(
                        text = hadith.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StorybookColors.StoryNavy
                    )
                }
            }
        }
    }
}

package com.nawawikids.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.model.ChapterVisual
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.HadithDataset
import com.nawawikids.app.model.HadithProgressRecord
import com.nawawikids.app.model.MapFilter
import com.nawawikids.app.model.StorybookChapters
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.RibbonBadge
import com.nawawikids.app.ui.components.StorybookBackground
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils
import kotlin.math.roundToInt

/**
 * Completely redesigned native Android 4-World Adventure Map:
 * 1) Modern Hero Top Bar (Profile Avatar + Rank + Streak + Stars + Library + Certificate)
 * 2) "Continue Your Adventure" Spotlight Card with saved step progress
 * 3) Illustrated 4-World Horizontal Explorer Deck (instant switching, no 6000dp endless scroll)
 * 4) Innovative Stepping-Stone Adventure Timeline with live step indicators, poster previews & voice badges
 */
@Composable
fun JourneyMapScreen(
    dataset: HadithDataset,
    progress: UserProgress,
    selectedWorldId: Int,
    mapFilter: MapFilter,
    onSelectWorld: (Int) -> Unit,
    onSelectFilter: (MapFilter) -> Unit,
    onOpenStation: (Int) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCertificate: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val total = dataset.hadiths.size.coerceAtLeast(42)
    val doneCount = progress.completedIds.size
    val overallPct = ((doneCount.toFloat() / total.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
    val frontierId = progress.frontierId(total)
    val frontierHadith = remember(dataset, frontierId) {
        dataset.hadiths.firstOrNull { it.id == frontierId } ?: dataset.hadiths.first()
    }

    val activeWorld = StorybookChapters.byId(selectedWorldId)
    val worldHadiths = remember(dataset, selectedWorldId) {
        dataset.hadiths.filter { it.stage == selectedWorldId }
    }
    val filteredHadiths = remember(worldHadiths, mapFilter, progress) {
        when (mapFilter) {
            MapFilter.ALL -> worldHadiths
            MapFilter.IN_PROGRESS -> worldHadiths.filter { progress.isUnlocked(it.id) && !progress.completedIds.contains(it.id) }
            MapFilter.COMPLETED -> worldHadiths.filter { progress.completedIds.contains(it.id) }
            MapFilter.NEEDS_RECITATION -> worldHadiths.filter { progress.isUnlocked(it.id) && !progress.recitedIds.contains(it.id) }
        }
    }

    val statusPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    StorybookBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Clean Hero Top Bar
            ModernHeroTopBar(
                progress = progress,
                doneCount = doneCount,
                total = total,
                overallPct = overallPct,
                topInset = statusPadding,
                onOpenProfile = onOpenProfile,
                onOpenLibrary = onOpenLibrary,
                onOpenCertificate = onOpenCertificate
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 14.dp,
                    bottom = 28.dp + navPadding
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. Hero "Continue Adventure" Spotlight Card
                item(key = "hero_spotlight") {
                    ContinueAdventureSpotlight(
                        frontierHadith = frontierHadith,
                        progress = progress,
                        doneCount = doneCount,
                        total = total,
                        onOpenStation = onOpenStation,
                        onOpenProfile = onOpenProfile,
                        onOpenCertificate = onOpenCertificate
                    )
                }

                // 3. 4-World Horizontal Illustrated Explorer Deck
                item(key = "world_deck_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "رحلة الأربعين النووية",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.StoryNavy
                            )
                            Text(
                                text = "اختر العالم لتستعرض محطاته وتقدّمك المحفوظ",
                                style = MaterialTheme.typography.bodySmall,
                                color = StorybookColors.StoryMuted
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = StorybookColors.TealSoft,
                            border = BorderStroke(1.5.dp, StorybookColors.TealBorder)
                        ) {
                            Text(
                                text = "${ArabicUtils.toArabicDigits(progress.totalXp)} XP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.TealDark,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                item(key = "world_deck") {
                    WorldExplorerDeck(
                        dataset = dataset,
                        progress = progress,
                        selectedWorldId = selectedWorldId,
                        onSelectWorld = onSelectWorld
                    )
                }

                // 4. Selected World Banner + Filter Chips
                item(key = "selected_world_banner") {
                    SelectedWorldSummaryCard(
                        world = activeWorld,
                        worldHadiths = worldHadiths,
                        progress = progress,
                        activeFilter = mapFilter,
                        onSelectFilter = onSelectFilter
                    )
                }

                // 5. Stepping-Stone Adventure Timeline Cards
                if (filteredHadiths.isEmpty()) {
                    item(key = "empty_filter") {
                        ToyCard(containerColor = Color.White) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "لا توجد محطات في هذا التصنيف حالياً!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.StoryNavy
                                )
                                CandyButton(
                                    onClick = { onSelectFilter(MapFilter.ALL) },
                                    topColor = activeWorld.light,
                                    bottomColor = activeWorld.main,
                                    borderColor = activeWorld.dark,
                                    lipColor = activeWorld.dark,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("اعرض كل محطات العالم", color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = filteredHadiths,
                        key = { _, h -> "milestone_${h.id}" }
                    ) { index, hadith ->
                        val isFirst = index == 0
                        val isLast = index == filteredHadiths.lastIndex
                        val prevCompleted = if (index > 0) progress.completedIds.contains(filteredHadiths[index - 1].id) else false
                        AdventureMilestoneRow(
                            hadith = hadith,
                            world = activeWorld,
                            record = progress.recordFor(hadith.id),
                            isUnlocked = progress.isUnlocked(hadith.id),
                            isCompleted = progress.completedIds.contains(hadith.id),
                            isFrontier = hadith.id == frontierId && !progress.completedIds.contains(hadith.id),
                            isFirst = isFirst,
                            isLast = isLast,
                            prevCompleted = prevCompleted,
                            onClick = { onOpenStation(hadith.id) }
                        )
                    }
                }

                // 6. Final Treasure Chest Reward Card
                item(key = "treasure_chest") {
                    TreasureRewardBanner(
                        heroName = progress.heroDisplayName,
                        doneCount = doneCount,
                        total = total,
                        onOpenCertificate = onOpenCertificate
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernHeroTopBar(
    progress: UserProgress,
    doneCount: Int,
    total: Int,
    overallPct: Int,
    topInset: androidx.compose.ui.unit.Dp,
    onOpenProfile: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenCertificate: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(top = topInset)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tappable Hero Avatar & Rank Pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(StorybookColors.CreamBg)
                        .border(1.5.dp, StorybookColors.GoldBorder, RoundedCornerShape(20.dp))
                        .clickable(onClick = onOpenProfile)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("open_profile_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssetImage(
                        assetPath = progress.avatarChoice.assetPath,
                        contentDescription = "ملف البطل",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, StorybookColors.GoldMain, CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (progress.hasSetProfileName) progress.heroDisplayName else "الأربعون النووية",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.StoryNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "تعديل الملف",
                                tint = StorybookColors.TealMain,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = progress.rank.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = StorybookColors.TealDark,
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Streak Pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = StorybookColors.CoralSoft,
                        border = BorderStroke(1.5.dp, StorybookColors.CoralBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "أيام متتالية",
                                tint = StorybookColors.CoralMain,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = ArabicUtils.toArabicDigits(progress.streakDays),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.CoralDeep
                            )
                        }
                    }

                    // Stars Pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = StorybookColors.GoldSoft,
                        border = BorderStroke(1.5.dp, StorybookColors.GoldMain)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "النجوم",
                                tint = StorybookColors.GoldMain,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${ArabicUtils.toArabicDigits(doneCount)}/${ArabicUtils.toArabicDigits(total)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.GoldDeep
                            )
                        }
                    }

                    // Library Icon Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(StorybookColors.TealSoft)
                            .border(1.5.dp, StorybookColors.TealMain, RoundedCornerShape(13.dp))
                            .clickable(onClick = onOpenLibrary)
                            .testTag("open_library_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = "مكتبة السنة",
                            tint = StorybookColors.TealDark,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Certificate Icon Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFFFB7185), Color(0xFFE11D48))))
                            .border(1.5.dp, StorybookColors.CoralDark, RoundedCornerShape(13.dp))
                            .clickable(onClick = onOpenCertificate)
                            .testTag("header_certificate_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = "شهادة البطل",
                            tint = StorybookColors.GoldLight,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Sleek Slim Journey Progress Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(StorybookColors.GoldSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((overallPct / 100f).coerceAtLeast(if (doneCount > 0) 0.03f else 0f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(StorybookColors.GoldMain, StorybookColors.EmeraldMain, StorybookColors.TealMain)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun ContinueAdventureSpotlight(
    frontierHadith: Hadith,
    progress: UserProgress,
    doneCount: Int,
    total: Int,
    onOpenStation: (Int) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCertificate: () -> Unit
) {
    val world = StorybookChapters.byId(frontierHadith.stage)
    val record = progress.recordFor(frontierHadith.id)
    val allDone = doneCount >= total

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F2F4C), RoundedCornerShape(28.dp))
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1E3A5F), Color(0xFF0F766E))
                )
            )
            .border(2.5.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(28.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Greeting + Profile nudge if unnamed
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (progress.hasSetProfileName) {
                            "أهلاً يا ${progress.heroDisplayName}! واصل مغامرتك"
                        } else {
                            "مرحباً بك في رحلة الأربعين النووية!"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.GoldLight
                    )
                    Text(
                        text = "${world.ord} • ${world.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCCFBF1)
                    )
                }

                if (!progress.hasSetProfileName) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, StorybookColors.GoldLight),
                        modifier = Modifier
                            .clickable(onClick = onOpenProfile)
                            .testTag("setup_profile_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, tint = StorybookColors.GoldLight, modifier = Modifier.size(14.dp))
                            Text("سجّل اسمك", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }

            // Current Active Hadith Spotlight Inner Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssetImage(
                    assetPath = frontierHadith.poster ?: world.sceneAsset,
                    contentDescription = frontierHadith.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, StorybookColors.GoldLight, RoundedCornerShape(16.dp))
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(shape = CircleShape, color = StorybookColors.GoldMain) {
                            Text(
                                text = frontierHadith.header,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.StoryInk,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "المحطة ${ArabicUtils.toArabicDigits(frontierHadith.id)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                    Text(
                        text = frontierHadith.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    // 4 Saved Step Mini Indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        StepMiniDot("استمع", record.listened, lightMode = false)
                        StepMiniDot("افهم", record.learned, lightMode = false)
                        StepMiniDot("سمّع", record.memorized, lightMode = false)
                        StepMiniDot("العب", record.quizCompleted, lightMode = false)
                    }
                }
            }

            // Big Play CTA
            CandyButton(
                onClick = {
                    if (allDone) onOpenCertificate() else onOpenStation(frontierHadith.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_journey_button"),
                topColor = Color(0xFFFDE047),
                bottomColor = StorybookColors.GoldMain,
                borderColor = Color(0xFFFDE047),
                lipColor = StorybookColors.GoldDark,
                contentColor = Color(0xFF451A03),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = if (allDone) Icons.Rounded.EmojiEvents else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF451A03),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = when {
                        allDone -> "مبارك! افتح شهادة الأربعين الآن"
                        doneCount == 0 && frontierHadith.id == 1 -> "ابدأ الرحلة من الحديث الأول"
                        else -> "أكمل المحطة ${ArabicUtils.toArabicDigits(frontierHadith.id)}: ${frontierHadith.title}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF451A03),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WorldExplorerDeck(
    dataset: HadithDataset,
    progress: UserProgress,
    selectedWorldId: Int,
    onSelectWorld: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(StorybookChapters.chapters, key = { it.id }) { world ->
            val worldIds = remember(dataset, world.id) {
                dataset.hadiths.filter { it.stage == world.id }.map { it.id }
            }
            val got = worldIds.count { progress.completedIds.contains(it) }
            val isSelected = world.id == selectedWorldId
            val isOpen = worldIds.firstOrNull()?.let { progress.isUnlocked(it) } ?: true

            Box(
                modifier = Modifier
                    .width(168.dp)
                    .height(126.dp)
                    .background(
                        if (isSelected) world.dark else world.border,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(bottom = if (isSelected) 5.dp else 3.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        width = if (isSelected) 3.dp else 1.5.dp,
                        color = if (isSelected) world.main else Color.White,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable { onSelectWorld(world.id) }
                    .testTag("world_card_${world.id}")
            ) {
                AssetImage(
                    assetPath = world.sceneAsset,
                    contentDescription = world.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark readable bottom scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Black.copy(alpha = 0.12f),
                                0.45f to Color.Black.copy(alpha = 0.35f),
                                1.0f to Color.Black.copy(alpha = 0.82f)
                            )
                        )
                )

                // Top Row: World pill & star count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) world.main else Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = world.ord,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if (!isOpen) Icons.Rounded.Lock else Icons.Rounded.Star,
                                contentDescription = null,
                                tint = if (!isOpen) StorybookColors.LockedInk else StorybookColors.GoldMain,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${ArabicUtils.toArabicDigits(got)}/${ArabicUtils.toArabicDigits(worldIds.size)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.StoryInk
                            )
                        }
                    }
                }

                // Bottom World Name & Range
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = world.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "الأحاديث ${world.shortRange}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StorybookColors.GoldLight
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedWorldSummaryCard(
    world: ChapterVisual,
    worldHadiths: List<Hadith>,
    progress: UserProgress,
    activeFilter: MapFilter,
    onSelectFilter: (MapFilter) -> Unit
) {
    val got = worldHadiths.count { progress.completedIds.contains(it.id) }
    val pct = if (worldHadiths.isEmpty()) 0f else got.toFloat() / worldHadiths.size.toFloat()

    AnimatedContent(targetState = world, label = "worldSwitch") { w ->
        ToyCard(
            containerColor = w.bg,
            borderColor = w.border,
            lipColor = w.light,
            cornerRadius = 24.dp,
            lipHeight = 5.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssetImage(
                    assetPath = w.guideAsset,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.5.dp, w.main, CircleShape)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = w.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = w.ink
                        )
                        Surface(shape = CircleShape, color = w.main) {
                            Text(
                                text = "${ArabicUtils.toArabicDigits(got)} / ${ArabicUtils.toArabicDigits(worldHadiths.size)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = w.story,
                        style = MaterialTheme.typography.bodySmall,
                        color = w.ink.copy(alpha = 0.88f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // World Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, w.border, CircleShape)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct.coerceAtLeast(if (got > 0) 0.05f else 0f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(w.main)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Quick Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(MapFilter.entries) { filter ->
                    val selected = filter == activeFilter
                    Surface(
                        shape = CircleShape,
                        color = if (selected) w.main else Color.White,
                        border = BorderStroke(1.5.dp, if (selected) w.dark else w.border),
                        modifier = Modifier
                            .clickable { onSelectFilter(filter) }
                            .testTag("map_filter_${filter.name}")
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (selected) Color.White else w.ink,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Innovative Stepping-Stone Adventure Milestone Row:
 * Connects a glowing Storybook Spine (Canvas) to a crisp, expressive Hadith Card
 * with thumbnail art, full title, saved 4-step progress pills, and mastery badges.
 */
@Composable
private fun AdventureMilestoneRow(
    hadith: Hadith,
    world: ChapterVisual,
    record: HadithProgressRecord,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    isFrontier: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    prevCompleted: Boolean,
    onClick: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "beaconPulse")
    val beaconScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (isFrontier) 1.09f else 1f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "beacon"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical Glowing Spine + Medallion Node
        Box(
            modifier = Modifier
                .width(58.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val solidStroke = 6.dp.toPx()
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)

                if (!isFirst) {
                    drawLine(
                        color = if (prevCompleted || isCompleted) world.main else world.border,
                        start = Offset(cx, 0f),
                        end = Offset(cx, cy),
                        strokeWidth = if (prevCompleted || isCompleted) solidStroke else 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = if (prevCompleted || isCompleted) null else dashEffect
                    )
                }
                if (!isLast) {
                    drawLine(
                        color = if (isCompleted) world.main else world.border,
                        start = Offset(cx, cy),
                        end = Offset(cx, size.height),
                        strokeWidth = if (isCompleted) solidStroke else 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = if (isCompleted) null else dashEffect
                    )
                }
            }

            // Medallion Circle
            val nodeLip = when {
                !isUnlocked -> StorybookColors.LockedLip
                isCompleted -> StorybookColors.GoldDark
                else -> world.dark
            }
            val nodeBrush = when {
                !isUnlocked -> Brush.verticalGradient(listOf(StorybookColors.LockedBg, StorybookColors.LockedBg))
                isCompleted -> Brush.verticalGradient(listOf(Color(0xFFFDE047), StorybookColors.GoldMain))
                isFrontier -> Brush.verticalGradient(listOf(world.light, world.main))
                else -> Brush.verticalGradient(listOf(Color.White, world.bg))
            }

            Box(
                modifier = Modifier
                    .size(if (isFrontier) 52.dp else 46.dp)
                    .scale(beaconScale)
                    .background(nodeLip, CircleShape)
                    .padding(bottom = 3.dp)
                    .clip(CircleShape)
                    .background(nodeBrush, CircleShape)
                    .border(
                        width = if (isFrontier) 3.dp else 2.dp,
                        color = if (isUnlocked) Color.White else Color(0xFFFBF6EB),
                        shape = CircleShape
                    )
                    .clickable(enabled = isUnlocked, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !isUnlocked -> Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "مغلقة",
                        tint = StorybookColors.LockedInk,
                        modifier = Modifier.size(18.dp)
                    )
                    isCompleted -> Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "مكتملة",
                        tint = Color(0xFF5C2C06),
                        modifier = Modifier.size(24.dp)
                    )
                    else -> Text(
                        text = ArabicUtils.toArabicDigits(hadith.id),
                        color = if (isFrontier) Color.White else world.ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // Expressive Station Milestone Card
        val cardBg = when {
            !isUnlocked -> Color(0xFFFAF5EA)
            isFrontier -> Color.White
            isCompleted -> Color(0xFFFFFDF5)
            else -> Color.White
        }
        val cardBorder = when {
            !isUnlocked -> Color(0xFFE6DAC3)
            isFrontier -> world.main
            isCompleted -> StorybookColors.GoldMain
            else -> world.border
        }
        val cardLip = when {
            !isUnlocked -> Color(0xFFDFD2B8)
            isFrontier -> world.dark
            isCompleted -> StorybookColors.GoldLip
            else -> world.border
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp)
                .background(cardLip, RoundedCornerShape(22.dp))
                .padding(bottom = if (isUnlocked) 4.dp else 2.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(cardBg)
                .border(if (isFrontier) 2.5.dp else 1.5.dp, cardBorder, RoundedCornerShape(22.dp))
                .clickable(enabled = isUnlocked, onClick = onClick)
                .minimumInteractiveComponentSize()
                .testTag("station_node_${hadith.id}")
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Poster thumbnail if available (Hadiths 1-13)
                    if (!hadith.poster.isNullOrBlank()) {
                        AssetImage(
                            assetPath = hadith.poster,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, cardBorder, RoundedCornerShape(14.dp))
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = hadith.header,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isUnlocked) world.dark else StorybookColors.LockedInk
                            )
                            if (isFrontier) {
                                Surface(shape = CircleShape, color = world.main) {
                                    Text(
                                        text = "محطتك الآن",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                                    )
                                }
                            } else if (isCompleted) {
                                Surface(shape = CircleShape, color = StorybookColors.EmeraldSoft) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = StorybookColors.EmeraldDark, modifier = Modifier.size(12.dp))
                                        Text("مكتمل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StorybookColors.EmeraldDark)
                                    }
                                }
                            }
                        }

                        Text(
                            text = hadith.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isUnlocked) StorybookColors.StoryInk else StorybookColors.LockedInk,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isUnlocked) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isFrontier) world.main else world.bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "افتح الحديث",
                                tint = if (isFrontier) Color.White else world.dark,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                // Bottom Saved Progress Strip (4 Steps + Recitation Accuracy Badge)
                if (isUnlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            StepMiniDot("استمع", record.listened, lightMode = true)
                            StepMiniDot("افهم", record.learned, lightMode = true)
                            StepMiniDot("سمّع", record.memorized, lightMode = true)
                            StepMiniDot("العب", record.quizCompleted, lightMode = true)
                        }

                        if (record.bestRecitationAccuracy > 0) {
                            Surface(
                                shape = CircleShape,
                                color = StorybookColors.VioletSoft,
                                border = BorderStroke(1.dp, StorybookColors.VioletBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Rounded.Mic, contentDescription = null, tint = StorybookColors.VioletDark, modifier = Modifier.size(11.dp))
                                    Text(
                                        text = "${ArabicUtils.toArabicDigits(record.bestRecitationAccuracy)}٪",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = StorybookColors.VioletDark
                                    )
                                }
                            }
                        } else if (record.puzzleCompleted || record.vocabCompleted) {
                            Icon(
                                imageVector = Icons.Rounded.Extension,
                                contentDescription = "لعبة منجزة",
                                tint = StorybookColors.TealMain,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "أكمل المحطة السابقة لفتح هذا الحديث",
                        style = MaterialTheme.typography.labelSmall,
                        color = StorybookColors.LockedInk
                    )
                }
            }
        }
    }
}

@Composable
private fun StepMiniDot(label: String, done: Boolean, lightMode: Boolean) {
    val bg = when {
        done && lightMode -> StorybookColors.EmeraldSoft
        done && !lightMode -> StorybookColors.EmeraldMain
        lightMode -> Color(0xFFF3F4F6)
        else -> Color.White.copy(alpha = 0.14f)
    }
    val textCol = when {
        done && lightMode -> StorybookColors.EmeraldDark
        done && !lightMode -> Color.White
        lightMode -> Color(0xFF6B7280)
        else -> Color.White.copy(alpha = 0.72f)
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (done) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = textCol,
                modifier = Modifier.size(10.dp)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textCol
        )
    }
}

@Composable
private fun TreasureRewardBanner(
    heroName: String,
    doneCount: Int,
    total: Int,
    onOpenCertificate: () -> Unit
) {
    val left = (total - doneCount).coerceAtLeast(0)
    ToyCard(
        containerColor = Color(0xFFFFF7D6),
        borderColor = Color(0xFFEAB308),
        lipColor = Color(0xFFCA8A04),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.verticalGradient(listOf(StorybookColors.GoldLight, StorybookColors.GoldMain)))
                    .border(2.5.dp, StorybookColors.GoldDark, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (left == 0) Icons.Rounded.AutoAwesome else Icons.Rounded.EmojiEvents,
                    contentDescription = "صندوق الكنز",
                    tint = StorybookColors.GoldDeep,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "صندوق الكنز وشهادة البطل",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.GoldDeep
                )
                Text(
                    text = if (left <= 0) {
                        "مبارك يا $heroName! أتممت الـ ${ArabicUtils.toArabicDigits(total)} حديثاً — خذ شهادتك الآن!"
                    } else {
                        "بقي لك ${ArabicUtils.toArabicDigits(left)} نجمة لفتح شهادتك الذهبية باسمك."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        CandyButton(
            onClick = onOpenCertificate,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("treasure_certificate_button"),
            topColor = Color(0xFFFB7185),
            bottomColor = Color(0xFFE11D48),
            borderColor = StorybookColors.CoralDark,
            lipColor = StorybookColors.CoralDeep,
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = StorybookColors.GoldLight, modifier = Modifier.size(18.dp))
            Text("عرض شهادة البطل", fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

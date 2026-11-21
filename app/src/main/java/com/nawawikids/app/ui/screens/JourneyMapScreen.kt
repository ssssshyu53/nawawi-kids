package com.nawawikids.app.ui.screens

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.model.ChapterVisual
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.HadithDataset
import com.nawawikids.app.model.StorybookChapters
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.RibbonBadge
import com.nawawikids.app.ui.components.StorybookBackground
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun JourneyMapScreen(
    dataset: HadithDataset,
    progress: UserProgress,
    onOpenStation: (Int) -> Unit,
    onNameChange: (String) -> Unit,
    onToggleTeacherMode: (Boolean) -> Unit,
    onToggleSfx: () -> Unit,
    onResetProgress: () -> Unit,
    onOpenCertificate: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    val total = dataset.hadiths.size.coerceAtLeast(42)
    val doneCount = progress.completedIds.size
    val pct = ((doneCount.toFloat() / total.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
    val frontier = progress.frontierId(total)

    val statusPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    StorybookBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sky & Arch Top Bar + Rainbow Star Progress Bar
            JourneyTopHeader(
                doneCount = doneCount,
                total = total,
                pct = pct,
                sfxMuted = progress.sfxMuted,
                topInset = statusPadding,
                onToggleSfx = onToggleSfx,
                onOpenLibrary = onOpenLibrary,
                onOpenCertificate = onOpenCertificate
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 14.dp,
                    bottom = 24.dp + navPadding
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Story Intro Card
                item(key = "intro_card") {
                    StoryIntroCard(
                        progress = progress,
                        doneCount = doneCount,
                        total = total,
                        frontier = frontier,
                        onNameChange = onNameChange,
                        onContinue = {
                            if (doneCount >= total) {
                                onOpenCertificate()
                            } else {
                                onOpenStation(frontier)
                            }
                        }
                    )
                }

                // 2. 4 Chapter Quick Jump Chips
                item(key = "chapter_chips") {
                    ChapterChipsGrid(
                        dataset = dataset,
                        completedIds = progress.completedIds,
                        onJumpToChapter = { chapterIndex ->
                            // Item indices: 0=intro, 1=chips, 2..5=chapters
                            scope.launch {
                                listState.animateScrollToItem(2 + chapterIndex)
                            }
                        }
                    )
                }

                // 3. The 4 Chapters with Winding Bezier Roads
                items(
                    count = StorybookChapters.chapters.size,
                    key = { idx -> "chapter_${StorybookChapters.chapters[idx].id}" }
                ) { idx ->
                    val chapter = StorybookChapters.chapters[idx]
                    val chapterHadiths = remember(dataset, chapter.id) {
                        dataset.hadiths.filter { it.stage == chapter.id }
                    }
                    ChapterWorldCard(
                        chapter = chapter,
                        hadiths = chapterHadiths,
                        progress = progress,
                        frontierId = frontier,
                        onOpenStation = onOpenStation
                    )
                }

                // 4. Final Treasure Chest Card
                item(key = "treasure_chest") {
                    TreasureChestCard(
                        heroName = progress.heroDisplayName,
                        doneCount = doneCount,
                        total = total,
                        onOpenCertificate = onOpenCertificate
                    )
                }

                // 5. Parent / Teacher Tools Card
                item(key = "teacher_tools") {
                    TeacherToolsCard(
                        teacherMode = progress.teacherMode,
                        onToggleTeacherMode = onToggleTeacherMode,
                        onRequestReset = { showResetDialog = true }
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = StorybookColors.CreamBg,
            title = {
                Text(
                    text = "إعادة بدء الرحلة؟",
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.StoryNavy
                )
            },
            text = {
                Text(
                    text = "سيتم مسح جميع النجوم المكتسبة والبدء من المحطة الأولى. هل أنت متأكد؟",
                    color = StorybookColors.StoryInk
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                    },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("نعم، ابدأ من جديد", color = StorybookColors.CoralDark, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء", color = StorybookColors.TealDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun JourneyTopHeader(
    doneCount: Int,
    total: Int,
    pct: Int,
    sfxMuted: Boolean,
    topInset: androidx.compose.ui.unit.Dp,
    onToggleSfx: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenCertificate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(StorybookColors.SkyLight, StorybookColors.SkySoft, StorybookColors.CreamBg)
                )
            )
            .padding(top = topInset)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.width(58.dp).height(44.dp)) {
                    AssetImage(
                        assetPath = "img/boy_guide.png",
                        contentDescription = "عمر صديق الرحلة",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.CenterStart)
                    )
                    AssetImage(
                        assetPath = "img/girl_guide.png",
                        contentDescription = "مريم صديقة الرحلة",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.CenterEnd)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الأربعون النووية للأطفال",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.StoryNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "استمع، افهم، سمّع بصوتك، والعب!",
                        style = MaterialTheme.typography.labelSmall,
                        color = StorybookColors.TealDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star Counter Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(2.dp, StorybookColors.GoldMain),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "النجوم",
                            tint = StorybookColors.GoldMain,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${ArabicUtils.toArabicDigits(doneCount)}/${ArabicUtils.toArabicDigits(total)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.GoldDeep
                        )
                    }
                }

                // Sunnah Library Button
                CandyButton(
                    onClick = onOpenLibrary,
                    modifier = Modifier.testTag("open_library_button"),
                    topColor = Color.White,
                    bottomColor = StorybookColors.TealSoft,
                    borderColor = StorybookColors.TealMain,
                    lipColor = StorybookColors.TealDark,
                    contentColor = StorybookColors.TealDark,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = "مكتبة السنة",
                        tint = StorybookColors.TealDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Sound FX Toggle
                CandyButton(
                    onClick = onToggleSfx,
                    modifier = Modifier.testTag("toggle_sfx_button"),
                    topColor = Color.White,
                    bottomColor = StorybookColors.SkySoft,
                    borderColor = StorybookColors.SkyAccent,
                    lipColor = StorybookColors.SkyDark,
                    contentColor = StorybookColors.SkyDark,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (sfxMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = if (sfxMuted) "تشغيل المؤثرات" else "كتم المؤثرات",
                        tint = StorybookColors.SkyDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Certificate Button
                CandyButton(
                    onClick = onOpenCertificate,
                    modifier = Modifier.testTag("header_certificate_button"),
                    topColor = Color(0xFFFB7185),
                    bottomColor = Color(0xFFE11D48),
                    borderColor = StorybookColors.CoralDark,
                    lipColor = StorybookColors.CoralDeep,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = "شهادة البطل",
                        tint = StorybookColors.GoldLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Rainbow Star Progress Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StorybookColors.GoldSoft)
                .border(1.dp, Color(0xFFFDE68A))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "مسار الرحلة:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = StorybookColors.GoldDark
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFFEAB308), CircleShape)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (pct / 100f).coerceAtLeast(if (pct > 0) 0.04f else 0f))
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(StorybookColors.GoldMain, StorybookColors.EmeraldMain, StorybookColors.TealMain)
                            )
                        )
                )
            }
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, StorybookColors.TealBorder)
            ) {
                Text(
                    text = "${ArabicUtils.toArabicDigits(pct)}٪",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.TealDark,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFFEAB308))
        )
    }
}

@Composable
private fun StoryIntroCard(
    progress: UserProgress,
    doneCount: Int,
    total: Int,
    frontier: Int,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val heroName = progress.heroDisplayName
    val greeting = if (heroName == "بطل") "أهلاً بك يا بطل!" else "أهلاً بك يا $heroName!"
    val ctaLabel = when {
        doneCount >= total -> "افتح صندوق الكنز الآن"
        doneCount == 0 && frontier == 1 -> "ابدأ الرحلة من المحطة الأولى"
        else -> "تابع الرحلة: المحطة ${ArabicUtils.toArabicDigits(frontier)}"
    }

    ToyCard(
        containerColor = Color(0xFFFFFDF7),
        borderColor = StorybookColors.GoldBorder,
        lipColor = StorybookColors.GoldLip
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.width(104.dp).height(76.dp)) {
                AssetImage(
                    assetPath = "img/boy_guide.png",
                    contentDescription = "عمر",
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color.White, CircleShape)
                        .align(Alignment.BottomStart)
                )
                AssetImage(
                    assetPath = "img/girl_guide.png",
                    contentDescription = "مريم",
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color.White, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RibbonBadge(text = "حكاية رحلة النجوم الأربعين")
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.StoryNavy
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "أنا عمر وهذه أختي مريم، وسنسافر معك عبر أربعة عوالم عجيبة! في كل محطة حديثٌ نبويّ جميل: نستمع له كلمةً بكلمة، نفهم معناه، نسمّعه بصوتنا مع كشف الأخطاء تلقائياً، ونلعب لنربح نجمة ذهبية.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF334155),
            lineHeight = 23.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "ما اسمك يا بطل؟ (سيظهر في الحكاية وعلى شهادتك)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = StorybookColors.SkyDark
        )
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = progress.studentName,
            onValueChange = onNameChange,
            placeholder = { Text("اكتب اسمك هنا") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StorybookColors.TealMain,
                unfocusedBorderColor = Color(0xFF7DD3FC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = StorybookColors.StoryNavy,
                unfocusedTextColor = StorybookColors.StoryNavy
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_name_input")
        )

        Spacer(Modifier.height(12.dp))

        CandyButton(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("continue_journey_button"),
            topColor = Color(0xFF34D399),
            bottomColor = Color(0xFF059669),
            borderColor = Color(0xFF047857),
            lipColor = Color(0xFF064E3B),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = StorybookColors.GoldLight,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = ctaLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ChapterChipsGrid(
    dataset: HadithDataset,
    completedIds: Set<Int>,
    onJumpToChapter: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val rows = StorybookChapters.chapters.chunked(2)
        rows.forEachIndexed { rowIdx, pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEachIndexed { colIdx, ch ->
                    val chIdx = rowIdx * 2 + colIdx
                    val ids = remember(dataset, ch.id) {
                        dataset.hadiths.filter { it.stage == ch.id }.map { it.id }
                    }
                    val got = ids.count { completedIds.contains(it) }
                    ToyCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chapter_chip_${ch.id}")
                            .clickable { onJumpToChapter(chIdx) },
                        containerColor = ch.bg,
                        borderColor = ch.border,
                        lipColor = ch.main,
                        cornerRadius = 20.dp,
                        lipHeight = 4.dp,
                        contentPadding = PaddingValues(11.dp)
                    ) {
                        Text(
                            text = ch.ord,
                            style = MaterialTheme.typography.labelSmall,
                            color = ch.ink.copy(alpha = 0.8f)
                        )
                        Text(
                            text = ch.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = ch.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = StorybookColors.GoldMain,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${ArabicUtils.toArabicDigits(got)} / ${ArabicUtils.toArabicDigits(ids.size)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = ch.ink
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterWorldCard(
    chapter: ChapterVisual,
    hadiths: List<Hadith>,
    progress: UserProgress,
    frontierId: Int,
    onOpenStation: (Int) -> Unit
) {
    val got = hadiths.count { progress.completedIds.contains(it.id) }
    val pct = if (hadiths.isEmpty()) 0f else got.toFloat() / hadiths.size.toFloat()
    val chapterOpen = hadiths.firstOrNull()?.let { progress.isUnlocked(it.id) } ?: true

    ToyCard(
        containerColor = chapter.bg,
        borderColor = chapter.border,
        lipColor = chapter.light,
        cornerRadius = 28.dp,
        lipHeight = 7.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        // Chapter Header with Illustrated Scene
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.White, chapter.bg)))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AssetImage(
                assetPath = chapter.sceneAsset,
                contentDescription = chapter.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(3.dp, Color.White, RoundedCornerShape(20.dp))
            )

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
                    Surface(shape = CircleShape, color = chapter.main) {
                        Text(
                            text = chapter.ord,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = chapter.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = chapter.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, chapter.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = StorybookColors.GoldMain,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${ArabicUtils.toArabicDigits(got)} / ${ArabicUtils.toArabicDigits(hadiths.size)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = chapter.ink
                        )
                    }
                }
            }

            // Storyteller speech box
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(2.dp, chapter.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssetImage(
                        assetPath = chapter.guideAsset,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(chapter.bg)
                    )
                    Text(
                        text = if (chapterOpen) chapter.story else "${chapter.story} (يُفتح هذا الفصل بعد إكمال الفصل السابق)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = chapter.ink,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Chapter progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, chapter.border, CircleShape)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = pct.coerceAtLeast(if (got > 0) 0.05f else 0f))
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(chapter.main)
                )
            }
        }

        // Winding Bezier Adventure Road
        WindingChapterRoad(
            chapter = chapter,
            hadiths = hadiths,
            progress = progress,
            frontierId = frontierId,
            onOpenStation = onOpenStation
        )
    }
}

@Composable
private fun WindingChapterRoad(
    chapter: ChapterVisual,
    hadiths: List<Hadith>,
    progress: UserProgress,
    frontierId: Int,
    onOpenStation: (Int) -> Unit
) {
    if (hadiths.isEmpty()) return

    val wave = remember { floatArrayOf(0f, 0.65f, 1f, 0.65f, 0f, -0.65f, -1f, -0.65f) }
    val gapDp = 148.dp
    val topDp = 82.dp
    val bottomDp = 96.dp
    val totalHeightDp = topDp + gapDp * (hadiths.size - 1) + bottomDp
    val decoAssets = remember { listOf("img/boy_guide.png", "img/book_guide.png", "img/girl_guide.png") }

    val infinite = rememberInfiniteTransition(label = "frontierBounce")
    val bounceOffsetDp by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "bounce"
    )

    // Use LTR coordinate space for deterministic Bezier geometry while keeping RTL text inside nodes
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeightDp)
                .padding(horizontal = 8.dp)
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val gapPx = with(density) { gapDp.toPx() }
            val topPx = with(density) { topDp.toPx() }
            val ampPx = ((widthPx - with(density) { 148.dp.toPx() }) / 2f).coerceIn(
                with(density) { 44.dp.toPx() },
                with(density) { 140.dp.toPx() }
            )

            val points = remember(hadiths, widthPx) {
                hadiths.mapIndexed { idx, h ->
                    val factor = wave[(h.id - 1) % wave.size]
                    val x = widthPx / 2f + factor * ampPx
                    val y = topPx + idx * gapPx
                    Triple(h, Offset(x, y), factor)
                }
            }

            // Road Bezier Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val outlineStroke = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                val bedStroke = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                val doneStroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                val shineStroke = Stroke(
                    width = 3.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 18f), 0f)
                )
                val dashStroke = Stroke(
                    width = 4.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 18f), 0f)
                )

                for (i in 0 until points.lastIndex) {
                    val p = points[i].second
                    val q = points[i + 1].second
                    val segPath = Path().apply {
                        moveTo(p.x, p.y)
                        cubicTo(p.x, p.y + gapPx / 2f, q.x, q.y - gapPx / 2f, q.x, q.y)
                    }
                    val isSegDone = progress.completedIds.contains(points[i].first.id)
                    drawPath(segPath, color = chapter.border, style = outlineStroke)
                    drawPath(segPath, color = Color.White.copy(alpha = 0.94f), style = bedStroke)
                    if (isSegDone) {
                        drawPath(segPath, color = chapter.main.copy(alpha = 0.85f), style = doneStroke)
                        drawPath(segPath, color = StorybookColors.GoldLight, style = shineStroke)
                    } else {
                        drawPath(segPath, color = chapter.main.copy(alpha = 0.42f), style = dashStroke)
                    }
                }
            }

            // Decorative floating companion medallions at road curves
            points.forEachIndexed { idx, (h, pos, factor) ->
                if (abs(factor) >= 0.99f && idx % 2 == 0) {
                    val decoSize = 54.dp
                    val decoSizePx = with(density) { decoSize.toPx() }
                    val decoX = widthPx / 2f - factor * ampPx * 0.78f - decoSizePx / 2f
                    val decoY = pos.y - decoSizePx / 2f
                    AssetImage(
                        assetPath = decoAssets[(h.id + chapter.id) % decoAssets.size],
                        contentDescription = null,
                        modifier = Modifier
                            .offset { IntOffset(decoX.roundToInt(), decoY.roundToInt()) }
                            .size(decoSize)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, chapter.border, CircleShape)
                    )
                }
            }

            // Interactive Station Nodes
            points.forEach { (h, pos, _) ->
                val isDone = progress.completedIds.contains(h.id)
                val isRecited = progress.recitedIds.contains(h.id)
                val isUnlocked = progress.isUnlocked(h.id)
                val isFrontier = h.id == frontierId && !isDone
                val nodeSize = if (isFrontier) 76.dp else 64.dp
                val wrapWidth = 132.dp
                val wrapWidthPx = with(density) { wrapWidth.toPx() }
                val halfNodePx = with(density) { (nodeSize / 2).toPx() }
                val extraTopBubblePx = if (isFrontier) with(density) { 36.dp.toPx() } else 0f

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (pos.x - wrapWidthPx / 2f).roundToInt(),
                                    y = (pos.y - halfNodePx - extraTopBubblePx).roundToInt()
                                )
                            }
                            .width(wrapWidth),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isFrontier) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StorybookColors.StoryNavy,
                                border = BorderStroke(2.dp, Color(0xFF38BDF8)),
                                modifier = Modifier
                                    .offset(y = bounceOffsetDp.dp)
                                    .padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = if (progress.completedIds.isEmpty()) "ابدأ من هنا!" else "محطتك التالية!",
                                    color = StorybookColors.GoldLight,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Circular 3D Station Button
                        val lipColor = when {
                            !isUnlocked -> StorybookColors.LockedLip
                            isDone -> StorybookColors.GoldDark
                            else -> chapter.dark
                        }
                        val fillBrush = when {
                            !isUnlocked -> Brush.verticalGradient(listOf(StorybookColors.LockedBg, StorybookColors.LockedBg))
                            isDone -> Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B)))
                            isFrontier -> Brush.verticalGradient(listOf(chapter.light, chapter.main))
                            else -> Brush.verticalGradient(listOf(Color.White, Color(0xFFFFFDF8)))
                        }
                        val borderCol = when {
                            !isUnlocked -> Color(0xFFFBF6EB)
                            isDone -> Color.White
                            isFrontier -> Color.White
                            else -> chapter.main
                        }

                        Box(
                            modifier = Modifier
                                .offset(y = if (isFrontier) bounceOffsetDp.dp else 0.dp)
                                .size(nodeSize)
                                .minimumInteractiveComponentSize()
                                .testTag("station_node_${h.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(lipColor, CircleShape)
                                    .padding(bottom = 5.dp)
                                    .clip(CircleShape)
                                    .background(fillBrush, CircleShape)
                                    .border(3.5.dp, borderCol, CircleShape)
                                    .clickable(enabled = isUnlocked) { onOpenStation(h.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    !isUnlocked -> Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "محطة مغلقة",
                                        tint = StorybookColors.LockedInk,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    isDone -> Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = "نجمة مكتسبة",
                                        tint = Color(0xFF5C2C06),
                                        modifier = Modifier.size(30.dp)
                                    )
                                    else -> Text(
                                        text = ArabicUtils.toArabicDigits(h.id),
                                        color = if (isFrontier) Color.White else chapter.ink,
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            // Done checkmark mini-badge
                            if (isDone) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(StorybookColors.EmeraldDark)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            // Voice-recited mastery badge
                            if (isRecited) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(StorybookColors.VioletMain)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Mic,
                                        contentDescription = "مُسمَّع بالصوت",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(5.dp))

                        // Station Title Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isUnlocked) Color.White else Color(0xFFFAF5EA),
                            border = BorderStroke(
                                1.5.dp,
                                if (isUnlocked) chapter.border else Color(0xFFE6DAC3)
                            ),
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = h.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isUnlocked) StorybookColors.StoryInk else StorybookColors.LockedInk,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreasureChestCard(
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
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(StorybookColors.GoldLight, StorybookColors.GoldMain)))
                    .border(3.dp, StorybookColors.GoldDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (left == 0) Icons.Rounded.AutoAwesome else Icons.Rounded.EmojiEvents,
                    contentDescription = "صندوق الكنز",
                    tint = StorybookColors.GoldDeep,
                    modifier = Modifier.size(38.dp)
                )
            }
            RibbonBadge(text = "نهاية الحكاية: صندوق الكنز")
            Text(
                text = if (left <= 0) {
                    "مبارك يا $heroName! جمعت النجوم الـ ${ArabicUtils.toArabicDigits(total)} كلها وانفتح صندوق الكنز. اضغط لتأخذ شهادتك!"
                } else {
                    "بقي لك ${ArabicUtils.toArabicDigits(left)} نجمة لتفتح الصندوق وتنال شهادة البطل باسمك!"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = StorybookColors.GoldDeep,
                textAlign = TextAlign.Center
            )
            CandyButton(
                onClick = onOpenCertificate,
                modifier = Modifier.testTag("treasure_certificate_button"),
                topColor = Color(0xFFFB7185),
                bottomColor = Color(0xFFE11D48),
                borderColor = StorybookColors.CoralDark,
                lipColor = StorybookColors.CoralDeep
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = StorybookColors.GoldLight,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "افتح صندوق الكنز (الشهادة)",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TeacherToolsCard(
    teacherMode: Boolean,
    onToggleTeacherMode: (Boolean) -> Unit,
    onRequestReset: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(2.dp, Color(0xFFFDE68A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                    Icon(
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        tint = StorybookColors.SkyDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "وضع المعلم / ولي الأمر (فتح كل المحطات)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.GoldDeep
                    )
                }
                Switch(
                    checked = teacherMode,
                    onCheckedChange = onToggleTeacherMode,
                    modifier = Modifier.testTag("teacher_mode_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = StorybookColors.SkyAccent
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onRequestReset,
                    modifier = Modifier.testTag("reset_progress_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        tint = StorybookColors.CoralDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "إعادة بدء الحكاية من البداية",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.CoralDark
                    )
                }
            }
        }
    }
}

package com.nawawikids.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.StorybookChapters
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.theme.AmiriFontFamily
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils

/**
 * Redesigned "أفهم الحديث" Storybook Learning Experience:
 * 1. Visual Poster Story Header + Refined Meaning Reflection Card
 * 2. Interactive "كنز الكلمات" Flashcard Explorer with Self-Test Flip Mode
 * 3. Interactive "سأطبّق في يومي!" Daily Action Habit Checklist
 * 4. Companion Encouragement & Elegant Archival Hadith Source Card
 */
@Composable
fun LearnTab(
    hadith: Hadith,
    heroName: String,
    avatarAsset: String,
    onOpenPoster: () -> Unit,
    onCheckLessonSfx: () -> Unit,
    onGoToMemorize: () -> Unit
) {
    val chapter = StorybookChapters.byId(hadith.stage)

    // Interactive state: vocab self-test flip mode & daily habit checklist
    var quizVocabMode by remember(hadith.id) { mutableStateOf(false) }
    val flippedVocabIndices = remember(hadith.id) { mutableStateListOf<Int>() }
    val pledgedLessons = remember(hadith.id) { mutableStateListOf<Int>() }
    var showSourceDetails by remember(hadith.id) { mutableStateOf(false) }

    val vocabPalettes = listOf(
        Triple(Color(0xFFF0FDFA), StorybookColors.TealMain, StorybookColors.TealDark),
        Triple(Color(0xFFFFF1F2), StorybookColors.CoralMain, StorybookColors.CoralDeep),
        Triple(Color(0xFFF5F3FF), StorybookColors.VioletMain, StorybookColors.VioletDeep),
        Triple(Color(0xFFF0F9FF), StorybookColors.SkyAccent, StorybookColors.SkyDark),
        Triple(Color(0xFFFFFBEB), StorybookColors.GoldMain, StorybookColors.GoldDeep)
    )

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        // 1. Visual Story Card: Illustrated Poster + Editorial Meaning Summary
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = BorderStroke(2.dp, chapter.border),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Hero Poster Banner with Soft Scrim
                val bannerAsset = hadith.poster?.takeIf { it.isNotBlank() } ?: chapter.sceneAsset
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                        .clickable(enabled = !hadith.poster.isNullOrBlank(), onClick = onOpenPoster)
                ) {
                    AssetImage(
                        assetPath = bannerAsset,
                        contentDescription = hadith.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x440F172A),
                                        StorybookColors.StoryNavy.copy(alpha = 0.90f)
                                    )
                                )
                            )
                    )

                    // Top-end zoom pill
                    if (!hadith.poster.isNullOrBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = StorybookColors.StoryNavy.copy(alpha = 0.72f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.ZoomIn, contentDescription = null, tint = StorybookColors.GoldLight, modifier = Modifier.size(14.dp))
                                Text("تكبير اللوحة", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }

                    // Bottom overlay title
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.AutoStories, contentDescription = null, tint = StorybookColors.GoldLight, modifier = Modifier.size(16.dp))
                            Text(
                                text = "قصّة الحديث ومعناه العام",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = StorybookColors.GoldLight
                            )
                        }
                        Text(
                            text = hadith.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Editorial Meaning Body (Calm Cream Paper + Accent Bar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFFDF7), Color(0xFFFFF8E7))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chapter.bg)
                                    .border(1.dp, chapter.border, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FormatQuote,
                                    contentDescription = null,
                                    tint = chapter.dark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "خلاصة الحديث في سطور",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = chapter.ink
                            )
                        }

                        Text(
                            text = hadith.meaning,
                            fontFamily = AmiriFontFamily,
                            fontSize = 22.sp,
                            lineHeight = 35.sp,
                            fontWeight = FontWeight.Bold,
                            color = StorybookColors.StoryInk,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 2. Interactive "كنز الكلمات" Flashcard Explorer
        if (hadith.ghareeb.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, StorybookColors.GoldBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StorybookColors.VioletSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = null,
                                    tint = StorybookColors.VioletDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "كنز الكلمات الغريبة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = StorybookColors.StoryNavy
                                )
                                Text(
                                    text = if (quizVocabMode) {
                                        "اضغط على أي بطاقة لتكشف معناها وتختبر نفسك!"
                                    } else {
                                        "${ArabicUtils.toArabicDigits(hadith.ghareeb.size)} كلمات مهمة لفهم الحديث"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StorybookColors.StoryMuted
                                )
                            }
                        }

                        // Self-test toggle pill
                        Surface(
                            shape = CircleShape,
                            color = if (quizVocabMode) StorybookColors.VioletMain else StorybookColors.VioletSoft,
                            border = BorderStroke(1.5.dp, if (quizVocabMode) StorybookColors.VioletDark else StorybookColors.VioletBorder),
                            modifier = Modifier
                                .clickable {
                                    quizVocabMode = !quizVocabMode
                                    flippedVocabIndices.clear()
                                    onCheckLessonSfx()
                                }
                                .testTag("vocab_self_test_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (quizVocabMode) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (quizVocabMode) Color.White else StorybookColors.VioletDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (quizVocabMode) "أظهر الكل" else "اختبر نفسك",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quizVocabMode) Color.White else StorybookColors.VioletDark
                                )
                            }
                        }
                    }

                    // 2-Column Adaptive Flashcards (pairs)
                    hadith.ghareeb.chunked(2).forEachIndexed { rowIdx, pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEachIndexed { colIdx, item ->
                                val idx = rowIdx * 2 + colIdx
                                val (bg, accent, ink) = vocabPalettes[idx % vocabPalettes.size]
                                val isHidden = quizVocabMode && !flippedVocabIndices.contains(idx)

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isHidden) Color(0xFFF8FAFC) else bg,
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isHidden) StorybookColors.VioletBorder else accent.copy(alpha = 0.55f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = quizVocabMode) {
                                            if (flippedVocabIndices.contains(idx)) {
                                                flippedVocabIndices.remove(idx)
                                            } else {
                                                flippedVocabIndices.add(idx)
                                                onCheckLessonSfx()
                                            }
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = accent.copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = item.w,
                                                fontFamily = AmiriFontFamily,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ink,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (isHidden) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.TouchApp,
                                                    contentDescription = null,
                                                    tint = StorybookColors.VioletMain,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = "اضغط لكشف المعنى",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StorybookColors.VioletDark
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = item.m,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = StorybookColors.StoryInk
                                            )
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 3. Interactive "طبّق الحديث في يومك!" Action Habit Checklist
        if (hadith.lessons.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, StorybookColors.TealBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StorybookColors.TealSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = StorybookColors.TealDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "ماذا نتعلّم ونطبّق اليوم؟",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = StorybookColors.StoryNavy
                                )
                                Text(
                                    text = "اضغط على كل فائدة تنوي تطبيقها اليوم يا $heroName!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StorybookColors.StoryMuted
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (pledgedLessons.size == hadith.lessons.size) StorybookColors.EmeraldSoft else StorybookColors.GoldSoft
                        ) {
                            Text(
                                text = "${ArabicUtils.toArabicDigits(pledgedLessons.size)}/${ArabicUtils.toArabicDigits(hadith.lessons.size)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (pledgedLessons.size == hadith.lessons.size) StorybookColors.EmeraldDark else StorybookColors.GoldDeep,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        hadith.lessons.forEachIndexed { idx, lesson ->
                            val checked = pledgedLessons.contains(idx)
                            val cardBg by animateColorAsState(
                                targetValue = if (checked) StorybookColors.EmeraldSoft else Color(0xFFF8FAFC),
                                label = "lessonBg"
                            )
                            val cardBorder by animateColorAsState(
                                targetValue = if (checked) StorybookColors.EmeraldMain else Color(0xFFE2E8F0),
                                label = "lessonBorder"
                            )

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = cardBg,
                                border = BorderStroke(1.5.dp, cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (checked) {
                                            pledgedLessons.remove(idx)
                                        } else {
                                            pledgedLessons.add(idx)
                                            onCheckLessonSfx()
                                        }
                                    }
                                    .testTag("lesson_pledge_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (checked) StorybookColors.EmeraldMain else Color.White)
                                            .border(
                                                2.dp,
                                                if (checked) StorybookColors.EmeraldDark else StorybookColors.TealMain,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (checked) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "سأطبقه",
                                                tint = Color.White,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        } else {
                                            Text(
                                                text = ArabicUtils.toArabicDigits(idx + 1),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Black,
                                                color = StorybookColors.TealDark
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = lesson,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (checked) StorybookColors.EmeraldDark else StorybookColors.StoryInk
                                        )
                                        if (checked) {
                                            Text(
                                                text = "رائع! نويتُ العمل بهذه الفائدة اليوم",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = StorybookColors.TealDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = pledgedLessons.size == hadith.lessons.size && hadith.lessons.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = StorybookColors.GoldSoft,
                            border = BorderStroke(1.5.dp, StorybookColors.GoldMain),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StorybookColors.GoldDark, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "بارك الله فيك يا $heroName! العمل بالعلم هو أجمل ثمرة للحفظ.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StorybookColors.GoldDeep
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3b. Optional Author Commentary by Imam An-Nawawi (for reading/benefit only, not required for recitation)
        if (hadith.authorNote.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF5F3FF),
                border = BorderStroke(1.5.dp, StorybookColors.VioletBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, StorybookColors.VioletBorder, RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MenuBook,
                                    contentDescription = null,
                                    tint = StorybookColors.VioletDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "تعقيب من المؤلف",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = StorybookColors.VioletDeep
                                )
                                Text(
                                    text = "الإمام النووي رحمه الله",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StorybookColors.VioletDark
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, StorybookColors.VioletBorder)
                        ) {
                            Text(
                                text = "للفائدة • غير مطلوب للحفظ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StorybookColors.VioletDark,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, StorybookColors.VioletBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = hadith.authorNoteText,
                            fontFamily = AmiriFontFamily,
                            fontSize = 19.sp,
                            lineHeight = 32.sp,
                            color = StorybookColors.StoryInk,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        // 4. Companion Reflection & Collapsible Authentic Source Badge
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFFFFFBEB),
            border = BorderStroke(1.5.dp, StorybookColors.GoldBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (hadith.source.isNotBlank()) showSourceDetails = !showSourceDetails }
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AssetImage(
                        assetPath = avatarAsset,
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, StorybookColors.GoldMain, CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "مصدر الحديث وتخريجه",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.GoldDeep
                        )
                        Text(
                            text = "من كتاب الأربعين النووية — اضغط لقراءة التخريج",
                            style = MaterialTheme.typography.labelSmall,
                            color = StorybookColors.StoryMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = StorybookColors.GoldDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                AnimatedVisibility(visible = showSourceDetails && hadith.source.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, StorybookColors.GoldBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = hadith.source,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = StorybookColors.StoryInk,
                            modifier = Modifier.padding(11.dp)
                        )
                    }
                }
            }
        }

        // Next Step CTA
        CandyButton(
            onClick = onGoToMemorize,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("goto_memorize_button"),
            topColor = Color(0xFFA78BFA),
            bottomColor = StorybookColors.VioletMain,
            borderColor = StorybookColors.VioletDark,
            lipColor = StorybookColors.VioletDeep
        ) {
            Text(
                text = "الخطوة ٣: هيا نحفظ ونسمّع بالصوت",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

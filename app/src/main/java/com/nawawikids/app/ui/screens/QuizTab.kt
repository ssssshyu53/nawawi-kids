package com.nawawikids.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.model.GhareebWord
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.QuizMode
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.AmiriFontFamily
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.ui.viewmodel.StationUiState
import com.nawawikids.app.util.ArabicUtils
import kotlin.random.Random

@Composable
fun QuizTab(
    hadith: Hadith,
    allHadiths: List<Hadith>,
    progress: UserProgress,
    stationState: StationUiState,
    onSelectQuizMode: (QuizMode) -> Unit,
    onSubmitAnswer: (Int, Int) -> Unit,
    onWordPuzzleSolved: () -> Unit,
    onVocabMatchSolved: () -> Unit,
    onGameCorrectSfx: () -> Unit,
    onGameWrongSfx: () -> Unit,
    onNextStation: () -> Unit,
    onBackToMap: () -> Unit,
    onOpenCertificate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 5 Varied Games & Exercises Horizontal Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameModePill(
                label = "أسئلة النجمة",
                icon = Icons.Rounded.Quiz,
                active = stationState.quizMode == QuizMode.HERO_QUIZ,
                completed = progress.completedIds.contains(hadith.id),
                activeColor = StorybookColors.CoralMain,
                activeDark = StorybookColors.CoralDark,
                onClick = { onSelectQuizMode(QuizMode.HERO_QUIZ) },
                modifier = Modifier.testTag("quiz_mode_hero")
            )
            GameModePill(
                label = "صواب أم خطأ",
                icon = Icons.Rounded.FactCheck,
                active = stationState.quizMode == QuizMode.TRUE_FALSE,
                completed = false,
                activeColor = StorybookColors.GoldMain,
                activeDark = StorybookColors.GoldDark,
                onClick = { onSelectQuizMode(QuizMode.TRUE_FALSE) },
                modifier = Modifier.testTag("quiz_mode_true_false")
            )
            GameModePill(
                label = "أكمل الفراغ",
                icon = Icons.Rounded.EditNote,
                active = stationState.quizMode == QuizMode.FILL_BLANK,
                completed = false,
                activeColor = StorybookColors.SkyAccent,
                activeDark = StorybookColors.SkyDark,
                onClick = { onSelectQuizMode(QuizMode.FILL_BLANK) },
                modifier = Modifier.testTag("quiz_mode_fill_blank")
            )
            GameModePill(
                label = "رتّب الكلمات",
                icon = Icons.Rounded.Extension,
                active = stationState.quizMode == QuizMode.WORD_PUZZLE,
                completed = progress.puzzleSolvedIds.contains(hadith.id),
                activeColor = StorybookColors.TealMain,
                activeDark = StorybookColors.TealDark,
                onClick = { onSelectQuizMode(QuizMode.WORD_PUZZLE) },
                modifier = Modifier.testTag("quiz_mode_puzzle")
            )
            GameModePill(
                label = "وصّل المعاني",
                icon = Icons.Rounded.Link,
                active = stationState.quizMode == QuizMode.VOCAB_MATCH,
                completed = progress.vocabSolvedIds.contains(hadith.id),
                activeColor = StorybookColors.VioletMain,
                activeDark = StorybookColors.VioletDark,
                onClick = { onSelectQuizMode(QuizMode.VOCAB_MATCH) },
                modifier = Modifier.testTag("quiz_mode_vocab")
            )
        }

        when (stationState.quizMode) {
            QuizMode.HERO_QUIZ -> HeroQuizSection(
                hadith = hadith,
                allHadiths = allHadiths,
                progress = progress,
                stationState = stationState,
                onSubmitAnswer = onSubmitAnswer,
                onNextStation = onNextStation,
                onBackToMap = onBackToMap,
                onOpenCertificate = onOpenCertificate
            )

            QuizMode.TRUE_FALSE -> TrueFalseChallengeGame(
                hadith = hadith,
                allHadiths = allHadiths,
                heroName = progress.heroDisplayName,
                onCorrectSfx = onGameCorrectSfx,
                onWrongSfx = onGameWrongSfx
            )

            QuizMode.FILL_BLANK -> FillInBlankGame(
                hadith = hadith,
                allHadiths = allHadiths,
                heroName = progress.heroDisplayName,
                onCorrectSfx = onGameCorrectSfx,
                onWrongSfx = onGameWrongSfx
            )

            QuizMode.WORD_PUZZLE -> WordOrderPuzzleGame(
                hadith = hadith,
                heroName = progress.heroDisplayName,
                alreadySolved = progress.puzzleSolvedIds.contains(hadith.id),
                onCorrectPick = onGameCorrectSfx,
                onWrongPick = onGameWrongSfx,
                onSolved = onWordPuzzleSolved
            )

            QuizMode.VOCAB_MATCH -> VocabMatchGame(
                hadith = hadith,
                allHadiths = allHadiths,
                heroName = progress.heroDisplayName,
                alreadySolved = progress.vocabSolvedIds.contains(hadith.id),
                onCorrectPair = onGameCorrectSfx,
                onWrongPair = onGameWrongSfx,
                onSolved = onVocabMatchSolved
            )
        }
    }
}

@Composable
private fun GameModePill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    completed: Boolean,
    activeColor: Color,
    activeDark: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CandyButton(
        onClick = onClick,
        modifier = modifier,
        topColor = if (active) activeColor else Color.White,
        bottomColor = if (active) activeDark else StorybookColors.CreamBg,
        borderColor = if (active) activeDark else StorybookColors.GoldBorder,
        lipColor = if (active) activeDark else StorybookColors.GoldLip,
        contentColor = if (active) Color.White else StorybookColors.StoryNavy,
        cornerRadius = 15.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = if (completed && !active) Icons.Rounded.CheckCircle else icon,
            contentDescription = null,
            tint = when {
                active -> Color.White
                completed -> StorybookColors.EmeraldDark
                else -> activeDark
            },
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = if (active) Color.White else StorybookColors.StoryNavy,
            maxLines = 1
        )
    }
}

@Composable
private fun HeroQuizSection(
    hadith: Hadith,
    allHadiths: List<Hadith>,
    progress: UserProgress,
    stationState: StationUiState,
    onSubmitAnswer: (Int, Int) -> Unit,
    onNextStation: () -> Unit,
    onBackToMap: () -> Unit,
    onOpenCertificate: () -> Unit
) {
    val isAlreadyDone = progress.completedIds.contains(hadith.id)
    val solvedCount = stationState.quizSolvedQuestions.size
    val totalQ = hadith.exercises.size
    val showBanner = isAlreadyDone || (totalQ > 0 && solvedCount >= totalQ)
    val letters = listOf("أ", "ب", "ج", "د")
    val cardThemes = listOf(
        Color(0xFFF0F9FF) to Color(0xFF7DD3FC),
        Color(0xFFFFFBEB) to Color(0xFFFCD34D),
        Color(0xFFF5F3FF) to Color(0xFFC4B5FD)
    )

    ToyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تحدّي الأبطال لفتح النجمة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.CoralDeep
                )
                Text(
                    text = "أجب على الأسئلة الثلاثة لتفوز بنجمة هذا الحديث!",
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
            Surface(
                shape = CircleShape,
                color = StorybookColors.GoldLight,
                border = BorderStroke(2.dp, Color(0xFFEAB308))
            ) {
                Text(
                    text = if (isAlreadyDone && solvedCount < totalQ) {
                        "${ArabicUtils.toArabicDigits(totalQ)}/${ArabicUtils.toArabicDigits(totalQ)} مكتسبة"
                    } else {
                        "${ArabicUtils.toArabicDigits(solvedCount)} / ${ArabicUtils.toArabicDigits(totalQ)}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.GoldDeep,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            hadith.exercises.forEachIndexed { qIdx, ex ->
                val (bg, border) = cardThemes[qIdx % cardThemes.size]
                val isSolved = stationState.quizSolvedQuestions.contains(qIdx)
                val wrongOpts = stationState.quizWrongOptionsByQuestion[qIdx] ?: emptySet()

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = bg,
                    border = BorderStroke(2.5.dp, border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.5.dp, StorybookColors.SkyAccent)
                            ) {
                                Text(
                                    text = "التدريب ${ArabicUtils.toArabicDigits(qIdx + 1)}: ${ex.badge}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = StorybookColors.SkyDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = when {
                                    isSolved -> "أحسنت!"
                                    wrongOpts.isNotEmpty() -> "جرّب خياراً آخر"
                                    else -> "اختر الإجابة"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    isSolved -> StorybookColors.EmeraldDark
                                    wrongOpts.isNotEmpty() -> StorybookColors.CoralDark
                                    else -> StorybookColors.GoldDark
                                }
                            )
                        }

                        Text(
                            text = ex.q,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.StoryInk
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ex.options.forEachIndexed { oIdx, opt ->
                                val isRightPicked = isSolved && oIdx == ex.correct
                                val isWrongPicked = wrongOpts.contains(oIdx)
                                val enabled = !isSolved && !isWrongPicked

                                val optBg = when {
                                    isRightPicked -> StorybookColors.EmeraldSoft
                                    isWrongPicked -> StorybookColors.CoralSoft
                                    else -> Color.White
                                }
                                val optBorder = when {
                                    isRightPicked -> StorybookColors.EmeraldDark
                                    isWrongPicked -> StorybookColors.CoralDark
                                    else -> StorybookColors.GoldBorder
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(optBorder, RoundedCornerShape(16.dp))
                                        .padding(bottom = 3.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(optBg)
                                        .border(2.dp, optBorder, RoundedCornerShape(16.dp))
                                        .clickable(enabled = enabled) { onSubmitAnswer(qIdx, oIdx) }
                                        .padding(12.dp)
                                        .testTag("quiz_q${qIdx}_opt${oIdx}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isRightPicked -> StorybookColors.EmeraldMain
                                                        isWrongPicked -> StorybookColors.CoralMain
                                                        else -> StorybookColors.GoldLight
                                                    }
                                                )
                                                .border(
                                                    1.5.dp,
                                                    if (isRightPicked || isWrongPicked) Color.White else Color(0xFFEAB308),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = letters.getOrElse(oIdx) { ArabicUtils.toArabicDigits(oIdx + 1) },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Black,
                                                color = if (isRightPicked || isWrongPicked) Color.White else StorybookColors.GoldDeep
                                            )
                                        }
                                        Text(
                                            text = opt,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = StorybookColors.StoryInk,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Feedback Box
                        AnimatedVisibility(visible = isSolved || wrongOpts.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSolved) StorybookColors.EmeraldSoft else StorybookColors.GoldSoft,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSolved) StorybookColors.EmeraldBorder else StorybookColors.GoldMain
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isSolved) {
                                        "ممتاز يا بطل! ${ex.feedback}"
                                    } else {
                                        "قريبة! راجع «افهم» أو «استمع» ثم جرّب إجابة أخرى."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSolved) StorybookColors.EmeraldDark else StorybookColors.GoldDeep,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Golden Star Celebration Banner
        AnimatedVisibility(visible = showBanner) {
            val total = allHadiths.size.coerceAtLeast(42)
            val allComplete = progress.completedIds.size >= total
            val nextHadith = allHadiths.firstOrNull { it.id == hadith.id + 1 }

            Column {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(StorybookColors.GoldLight, Color(0xFFECFDF5), StorybookColors.EmeraldSoft)
                            )
                        )
                        .border(3.dp, StorybookColors.EmeraldMain, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(StorybookColors.EmeraldMain, StorybookColors.EmeraldDark)))
                                .border(3.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = StorybookColors.GoldLight,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = if (hadith.id < total) {
                                "أحسنت يا ${progress.heroDisplayName}! ربحت نجمة هذه المحطة"
                            } else {
                                "مبارك يا ${progress.heroDisplayName}! وصلت إلى آخر محطة في الحكاية"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.EmeraldDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = when {
                                nextHadith != null -> "انفتحت المحطة التالية: ${nextHadith.header} «${nextHadith.title}»"
                                allComplete -> "أتممت جميع الأحاديث الـ ٤٢! يمكنك الآن فتح صندوق الكنز وأخذ شهادتك."
                                else -> "بعض المحطات السابقة بانتظارك على الخريطة لإكمال النجوم الـ ٤٢!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = StorybookColors.StoryInk,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CandyButton(
                                onClick = onBackToMap,
                                modifier = Modifier.weight(1f),
                                topColor = Color(0xFF38BDF8),
                                bottomColor = Color(0xFF0284C7),
                                borderColor = StorybookColors.SkyDark,
                                lipColor = Color(0xFF075985),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Rounded.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("الخريطة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color.White)
                            }

                            if (nextHadith != null) {
                                CandyButton(
                                    onClick = onNextStation,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("unlock_next_station_button"),
                                    topColor = Color(0xFF34D399),
                                    bottomColor = Color(0xFF059669),
                                    borderColor = Color(0xFF047857),
                                    lipColor = Color(0xFF064E3B),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                                ) {
                                    Text("المحطة التالية", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color.White)
                                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            } else if (allComplete) {
                                CandyButton(
                                    onClick = onOpenCertificate,
                                    modifier = Modifier.weight(1f),
                                    topColor = Color(0xFFFB7185),
                                    bottomColor = Color(0xFFE11D48),
                                    borderColor = StorybookColors.CoralDark,
                                    lipColor = StorybookColors.CoralDeep,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = StorybookColors.GoldLight, modifier = Modifier.size(16.dp))
                                    Text("الشهادة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Mini-Game #2: Word Order Puzzle ("رتّب كلمات الحديث")
 * Picks a memorable 6–8 word excerpt from the current hadith, scrambles it,
 * and lets the child tap word tiles in order to reconstruct the prophetic saying!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordOrderPuzzleGame(
    hadith: Hadith,
    heroName: String,
    alreadySolved: Boolean,
    onCorrectPick: () -> Unit,
    onWrongPick: () -> Unit,
    onSolved: () -> Unit
) {
    var shuffleSeed by remember(hadith.id) { mutableStateOf(1) }
    val targetWords = remember(hadith.id, shuffleSeed) {
        val rawWords = hadith.words.map { it.t }
        val quoteStart = rawWords.indexOfFirst { it.contains("«") }.coerceAtLeast(0)
        val availableLen = (rawWords.size - quoteStart).coerceAtLeast(1)
        val offset = if (availableLen > 10) ((shuffleSeed - 1) * 5) % (availableLen - 6).coerceAtLeast(1) else 0
        val start = (quoteStart + offset).coerceAtMost((rawWords.size - 5).coerceAtLeast(0))
        rawWords.drop(start).take(7.coerceAtMost(rawWords.size - start))
    }

    val scrambled = remember(hadith.id, shuffleSeed) {
        val indexed = targetWords.mapIndexed { idx, w -> idx to w }
        val rng = Random(hadith.id * 7919 + shuffleSeed * 31)
        val shuffled = indexed.shuffled(rng)
        if (shuffled.map { it.first } == indexed.map { it.first } && shuffled.size > 1) {
            shuffled.reversed()
        } else {
            shuffled
        }
    }

    val chosenIndices = remember(hadith.id, shuffleSeed) { mutableStateListOf<Int>() }
    var wrongFlashId by remember(hadith.id, shuffleSeed) { mutableStateOf<Int?>(null) }
    var solvedNow by remember(hadith.id, shuffleSeed) { mutableStateOf(false) }

    ToyCard(
        containerColor = Color(0xFFF0FDFA),
        borderColor = StorybookColors.TealBorder,
        lipColor = StorybookColors.TealMain
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "لعبة ترتيب كلمات الحديث",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.TealDark
                )
                Text(
                    text = "اضغط على الكلمات المبعثرة بالترتيب الصحيح لتبني جملة الحديث!",
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
            CandyButton(
                onClick = { shuffleSeed++ },
                topColor = Color.White,
                bottomColor = StorybookColors.GoldSoft,
                borderColor = StorybookColors.GoldMain,
                lipColor = StorybookColors.GoldDark,
                contentColor = StorybookColors.GoldDeep,
                cornerRadius = 13.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "إعادة", tint = StorybookColors.GoldDeep, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        // Built sentence tray
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(2.5.dp, if (solvedNow) StorybookColors.EmeraldMain else StorybookColors.TealMain),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "الجملة التي جمعتها (${ArabicUtils.toArabicDigits(chosenIndices.size)} / ${ArabicUtils.toArabicDigits(targetWords.size)}):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.TealDark
                )
                Spacer(Modifier.height(10.dp))
                if (chosenIndices.isEmpty()) {
                    Text(
                        text = "اضغط على أول كلمة صحيحة من البطاقات بالأسفل...",
                        style = MaterialTheme.typography.bodySmall,
                        color = StorybookColors.StoryMuted
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chosenIndices.forEach { origIdx ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StorybookColors.EmeraldSoft)
                                    .border(1.5.dp, StorybookColors.EmeraldMain, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = targetWords[origIdx],
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.EmeraldDark
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Scrambled word tiles bank
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            scrambled.forEach { (origIdx, wordText) ->
                val isUsed = chosenIndices.contains(origIdx)
                val isWrong = wrongFlashId == origIdx
                if (!isUsed) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isWrong) StorybookColors.CoralDark else StorybookColors.GoldDark,
                                RoundedCornerShape(15.dp)
                            )
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (isWrong) StorybookColors.CoralSoft else StorybookColors.GoldSoft)
                            .border(
                                2.dp,
                                if (isWrong) StorybookColors.CoralMain else StorybookColors.GoldMain,
                                RoundedCornerShape(15.dp)
                            )
                            .clickable {
                                val expectedNext = chosenIndices.size
                                if (origIdx == expectedNext) {
                                    wrongFlashId = null
                                    chosenIndices.add(origIdx)
                                    if (chosenIndices.size == targetWords.size) {
                                        solvedNow = true
                                        onSolved()
                                    } else {
                                        onCorrectPick()
                                    }
                                } else {
                                    wrongFlashId = origIdx
                                    onWrongPick()
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = wordText,
                            fontFamily = AmiriFontFamily,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWrong) StorybookColors.CoralDeep else StorybookColors.GoldDeep
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = wrongFlashId != null && !solvedNow) {
            Column {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "قريبة يا بطل! فكّر في الكلمة التي تأتي التالية مباشرة في الحديث.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.CoralDark
                )
            }
        }

        AnimatedVisibility(visible = solvedNow || alreadySolved) {
            Column {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StorybookColors.EmeraldSoft,
                    border = BorderStroke(2.dp, StorybookColors.EmeraldMain),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StorybookColors.GoldMain)
                        Text(
                            text = if (solvedNow) "رائع يا $heroName! رتّبت كلمات الحديث بدقة مذهلة!" else "أتممت تحدي الترتيب لهذا الحديث من قبل — أحسنت!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.EmeraldDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Mini-Game #3: Vocabulary Pair Match ("وصّل الكلمة بمعناها")
 */
@Composable
private fun VocabMatchGame(
    hadith: Hadith,
    allHadiths: List<Hadith>,
    heroName: String,
    alreadySolved: Boolean,
    onCorrectPair: () -> Unit,
    onWrongPair: () -> Unit,
    onSolved: () -> Unit
) {
    val pairs: List<GhareebWord> = remember(hadith.id) {
        val list = hadith.ghareeb.toMutableList()
        if (list.size < 3) {
            val extras = allHadiths
                .filter { it.id != hadith.id }
                .flatMap { it.ghareeb }
                .filter { cand -> list.none { it.w == cand.w } }
            for (ex in extras) {
                if (list.size >= 4) break
                list.add(ex)
            }
        }
        list.take(4)
    }

    var gameSeed by remember(hadith.id) { mutableStateOf(1) }
    val shuffledMeanings = remember(hadith.id, gameSeed) {
        val rng = Random(hadith.id * 3571 + gameSeed * 19)
        pairs.mapIndexed { idx, p -> idx to p.m }.shuffled(rng)
    }

    var selectedWordIdx by remember(hadith.id, gameSeed) { mutableStateOf<Int?>(null) }
    val matchedIds = remember(hadith.id, gameSeed) { mutableStateListOf<Int>() }
    var feedbackWrong by remember(hadith.id, gameSeed) { mutableStateOf(false) }
    var solvedNow by remember(hadith.id, gameSeed) { mutableStateOf(false) }

    LaunchedEffect(hadith.id) {
        selectedWordIdx = null
        matchedIds.clear()
        feedbackWrong = false
        solvedNow = false
    }

    ToyCard(
        containerColor = Color(0xFFFAF5FF),
        borderColor = StorybookColors.VioletBorder,
        lipColor = StorybookColors.VioletMain
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "لعبة وصّل الكلمة بمعناها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.VioletDeep
                )
                Text(
                    text = "اختر كلمة من العمود الأول، ثم اضغط على معناها الصحيح!",
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
            CandyButton(
                onClick = { gameSeed++ },
                topColor = Color.White,
                bottomColor = StorybookColors.GoldSoft,
                borderColor = StorybookColors.GoldMain,
                lipColor = StorybookColors.GoldDark,
                contentColor = StorybookColors.GoldDeep,
                cornerRadius = 13.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "إعادة", tint = StorybookColors.GoldDeep, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Column 1: Words
            Column(
                modifier = Modifier.weight(0.44f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "الكلمة",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.VioletDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                pairs.forEachIndexed { idx, item ->
                    val isMatched = matchedIds.contains(idx)
                    val isSelected = selectedWordIdx == idx
                    val bg = when {
                        isMatched -> StorybookColors.EmeraldSoft
                        isSelected -> StorybookColors.VioletMain
                        else -> Color.White
                    }
                    val border = when {
                        isMatched -> StorybookColors.EmeraldMain
                        isSelected -> StorybookColors.VioletDeep
                        else -> StorybookColors.VioletBorder
                    }
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = bg,
                        border = BorderStroke(2.dp, border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isMatched) {
                                feedbackWrong = false
                                selectedWordIdx = idx
                            }
                    ) {
                        Text(
                            text = item.w,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = when {
                                isMatched -> StorybookColors.EmeraldDark
                                isSelected -> Color.White
                                else -> StorybookColors.VioletDeep
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            // Column 2: Meanings
            Column(
                modifier = Modifier.weight(0.56f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "المعنى",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.TealDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                shuffledMeanings.forEach { (origIdx, meaning) ->
                    val isMatched = matchedIds.contains(origIdx)
                    val bg = if (isMatched) StorybookColors.EmeraldSoft else StorybookColors.GoldSoft
                    val border = if (isMatched) StorybookColors.EmeraldMain else StorybookColors.GoldBorder

                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = bg,
                        border = BorderStroke(2.dp, border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isMatched && selectedWordIdx != null) {
                                val sel = selectedWordIdx ?: return@clickable
                                if (sel == origIdx) {
                                    feedbackWrong = false
                                    matchedIds.add(origIdx)
                                    selectedWordIdx = null
                                    if (matchedIds.size == pairs.size) {
                                        solvedNow = true
                                        onSolved()
                                    } else {
                                        onCorrectPair()
                                    }
                                } else {
                                    feedbackWrong = true
                                    onWrongPair()
                                }
                            }
                    ) {
                        Text(
                            text = meaning,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatched) StorybookColors.EmeraldDark else StorybookColors.GoldDeep,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = feedbackWrong) {
            Column {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "هذا ليس معنى الكلمة المختارة — جرّب معنىً آخر يا بطل!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.CoralDark
                )
            }
        }

        AnimatedVisibility(visible = solvedNow || alreadySolved) {
            Column {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StorybookColors.EmeraldSoft,
                    border = BorderStroke(2.dp, StorybookColors.EmeraldMain),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StorybookColors.GoldMain)
                        Text(
                            text = if (solvedNow) "ذكي جداً يا $heroName! وصلت جميع الكلمات بمعانيها بنجاح!" else "أتممت لعبة المعاني لهذا الحديث من قبل — بطل!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.EmeraldDark
                        )
                    }
                }
            }
        }
    }
}

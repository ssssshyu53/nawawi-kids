package com.nawawikids.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.speech.ArabicSpeechMatcher
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.AmiriFontFamily
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils
import kotlin.random.Random

private data class TrueFalseCardItem(
    val statement: String,
    val isTrue: Boolean,
    val explanation: String,
    val tag: String
)

private data class FillBlankQuestion(
    val beforeWords: String,
    val afterWords: String,
    val correctWord: String,
    val options: List<String>
)

/**
 * Interactive Mini-Game #4: Rapid-Fire True / False ("صواب أم خطأ؟")
 * Dynamically creates 4 authentic True/False statements per Hadith from its real
 * lessons, meanings, and cross-hadith distractors!
 */
@Composable
fun TrueFalseChallengeGame(
    hadith: Hadith,
    allHadiths: List<Hadith>,
    heroName: String,
    onCorrectSfx: () -> Unit,
    onWrongSfx: () -> Unit
) {
    var seed by remember(hadith.id) { mutableIntStateOf(1) }

    val items: List<TrueFalseCardItem> = remember(hadith.id, seed) {
        val rng = Random(hadith.id * 4099 + seed * 131)
        val built = mutableListOf<TrueFalseCardItem>()

        // 1. True card from real lesson
        hadith.lessons.shuffled(rng).firstOrNull()?.let { lesson ->
            built.add(
                TrueFalseCardItem(
                    statement = "من الدروس التي نتعلّمها من هذا الحديث: «$lesson».",
                    isTrue = true,
                    explanation = "صحيح! هذه من الفوائد الجميلة التي يعلّمنا إياها «${hadith.title}».",
                    tag = "فوائد الحديث"
                )
            )
        }

        // 2. False card using a swapped meaning from another hadith's ghareeb word
        val ownGhareeb = hadith.ghareeb.shuffled(rng).firstOrNull()
        val otherGhareeb = allHadiths
            .filter { it.id != hadith.id }
            .flatMap { it.ghareeb }
            .filter { it.m != ownGhareeb?.m && it.w != ownGhareeb?.w }
            .shuffled(rng)
            .firstOrNull()

        if (ownGhareeb != null && otherGhareeb != null) {
            built.add(
                TrueFalseCardItem(
                    statement = "معنى كلمة «${ownGhareeb.w}» في الحديث هو: «${otherGhareeb.m}».",
                    isTrue = false,
                    explanation = "خطأ! المعنى الصحيح لكلمة «${ownGhareeb.w}» هو: «${ownGhareeb.m}».",
                    tag = "معاني الكلمات"
                )
            )
        }

        // 3. True card from a second real ghareeb word or meaning
        val secondGhareeb = hadith.ghareeb.firstOrNull { it.w != ownGhareeb?.w } ?: ownGhareeb
        if (secondGhareeb != null) {
            built.add(
                TrueFalseCardItem(
                    statement = "كلمة «${secondGhareeb.w}» معناها: «${secondGhareeb.m}».",
                    isTrue = true,
                    explanation = "أحسنت! «${secondGhareeb.w}» تعني فعلاً «${secondGhareeb.m}».",
                    tag = "كنز المفردات"
                )
            )
        }

        // 4. False card mixing another hadith's title/topic
        val otherHadith = allHadiths.filter { it.id != hadith.id }.shuffled(rng).firstOrNull()
        if (otherHadith != null) {
            built.add(
                TrueFalseCardItem(
                    statement = "العنوان الرئيسي لهذا الحديث هو: «${otherHadith.title}».",
                    isTrue = false,
                    explanation = "خطأ! عنوان هذا الحديث هو «${hadith.title}» (${hadith.header}).",
                    tag = "معرفة الحديث"
                )
            )
        }

        built.shuffled(rng).take(4)
    }

    val answers = remember(hadith.id, seed) { mutableStateMapOf<Int, Boolean>() }
    val correctCount = items.indices.count { i -> answers[i] == items[i].isTrue }
    val allAnswered = items.isNotEmpty() && answers.size == items.size

    ToyCard(
        containerColor = StorybookColors.GoldSoft,
        borderColor = StorybookColors.GoldBorder,
        lipColor = StorybookColors.GoldMain
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تحدّي صواب أم خطأ؟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.GoldDeep
                )
                Text(
                    text = "اقرأ كل جملة بعناية واختر (صح ✓) أو (خطأ ✗) بسرعة!",
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
            CandyButton(
                onClick = { seed++ },
                topColor = StorybookColors.SurfaceWhite,
                bottomColor = StorybookColors.GoldSoft,
                borderColor = StorybookColors.GoldMain,
                lipColor = StorybookColors.GoldLip,
                contentColor = StorybookColors.GoldDeep,
                cornerRadius = 13.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "أسئلة جديدة", tint = StorybookColors.GoldDeep, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEachIndexed { idx, item ->
                val userChoice = answers[idx]
                val isAnswered = userChoice != null
                val isCorrect = userChoice == item.isTrue

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = StorybookColors.SurfaceWhite,
                    border = BorderStroke(
                        2.dp,
                        when {
                            !isAnswered -> StorybookColors.GoldBorder
                            isCorrect -> StorybookColors.EmeraldMain
                            else -> StorybookColors.CoralMain
                        }
                    ),
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
                                color = StorybookColors.GoldSoft
                            ) {
                                Text(
                                    text = "${ArabicUtils.toArabicDigits(idx + 1)} • ${item.tag}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = StorybookColors.GoldDeep,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                            if (isAnswered) {
                                Text(
                                    text = if (isCorrect) "إجابة صحيحة!" else "تعلّمت معلومة جديدة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isCorrect) StorybookColors.EmeraldDark else StorybookColors.CoralDark
                                )
                            }
                        }

                        Text(
                            text = item.statement,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = StorybookColors.StoryInk
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val truePicked = userChoice == true
                            Surface(
                                shape = RoundedCornerShape(15.dp),
                                color = when {
                                    isAnswered && item.isTrue -> StorybookColors.EmeraldMain
                                    truePicked && !item.isTrue -> StorybookColors.CoralSoft
                                    else -> StorybookColors.EmeraldSoft
                                },
                                border = BorderStroke(
                                    2.dp,
                                    if (isAnswered && item.isTrue) StorybookColors.EmeraldDark else StorybookColors.EmeraldBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isAnswered) {
                                        answers[idx] = true
                                        if (item.isTrue) onCorrectSfx() else onWrongSfx()
                                    }
                                    .testTag("tf_true_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (isAnswered && item.isTrue) Color.White else StorybookColors.EmeraldDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = "صح",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (isAnswered && item.isTrue) Color.White else StorybookColors.EmeraldDark
                                    )
                                }
                            }

                            val falsePicked = userChoice == false
                            Surface(
                                shape = RoundedCornerShape(15.dp),
                                color = when {
                                    isAnswered && !item.isTrue -> StorybookColors.EmeraldMain
                                    falsePicked && item.isTrue -> StorybookColors.CoralMain
                                    else -> StorybookColors.CoralSoft
                                },
                                border = BorderStroke(
                                    2.dp,
                                    if (isAnswered && !item.isTrue) StorybookColors.EmeraldDark else StorybookColors.CoralBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isAnswered) {
                                        answers[idx] = false
                                        if (!item.isTrue) onCorrectSfx() else onWrongSfx()
                                    }
                                    .testTag("tf_false_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = if ((isAnswered && !item.isTrue) || (falsePicked && item.isTrue)) Color.White else StorybookColors.CoralDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = "خطأ",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if ((isAnswered && !item.isTrue) || (falsePicked && item.isTrue)) Color.White else StorybookColors.CoralDark
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isAnswered) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCorrect) StorybookColors.EmeraldSoft else StorybookColors.SkySoft,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) StorybookColors.EmeraldDark else StorybookColors.SkyDark,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = allAnswered) {
            Column {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = StorybookColors.EmeraldSoft,
                    border = BorderStroke(2.dp, StorybookColors.EmeraldMain),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StorybookColors.GoldDark)
                        Text(
                            text = "رائع يا $heroName! نتيجتك ${ArabicUtils.toArabicDigits(correctCount)} من ${ArabicUtils.toArabicDigits(items.size)} — اضغط التحديث لجولة جديدة!",
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
 * Interactive Mini-Game #5: Fill in the Missing Word ("أكمل الفراغ في الحديث")
 * Picks 3 memorable excerpts from the Hadith text, blanks out a key Arabic word,
 * and presents 4 Amiri-script choices!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillInBlankGame(
    hadith: Hadith,
    allHadiths: List<Hadith>,
    heroName: String,
    onCorrectSfx: () -> Unit,
    onWrongSfx: () -> Unit
) {
    var seed by remember(hadith.id) { mutableIntStateOf(1) }

    val questions: List<FillBlankQuestion> = remember(hadith.id, seed) {
        val rng = Random(hadith.id * 6133 + seed * 97)
        val words = hadith.words.map { it.t }
        if (words.size < 6) return@remember emptyList()

        // Prefer content words (length >= 4 normalized) inside the prophetic text
        val quoteStart = words.indexOfFirst { it.contains("«") }.coerceAtLeast(0)
        val candidates = (quoteStart until words.size).filter { i ->
            ArabicSpeechMatcher.normalize(words[i]).length >= 4
        }.shuffled(rng)

        val pickedIndices = mutableListOf<Int>()
        for (idx in candidates) {
            if (pickedIndices.none { kotlin.math.abs(it - idx) < 4 }) {
                pickedIndices.add(idx)
            }
            if (pickedIndices.size == 3) break
        }
        if (pickedIndices.isEmpty()) pickedIndices.add((words.size / 2).coerceIn(1, words.lastIndex))

        val distractorPool = (
            words.filter { ArabicSpeechMatcher.normalize(it).length >= 4 } +
                allHadiths.filter { it.id != hadith.id }.take(8).flatMap { h ->
                    h.words.map { it.t }.filter { ArabicSpeechMatcher.normalize(it).length >= 4 }
                }
            ).distinctBy { ArabicSpeechMatcher.normalize(it) }

        pickedIndices.sorted().map { targetIdx ->
            val start = (targetIdx - 3).coerceAtLeast(0)
            val end = (targetIdx + 3).coerceAtMost(words.lastIndex)
            val before = if (targetIdx > start) words.subList(start, targetIdx).joinToString(" ") else ""
            val after = if (targetIdx < end) words.subList(targetIdx + 1, end + 1).joinToString(" ") else ""
            val correct = words[targetIdx]
            val correctNorm = ArabicSpeechMatcher.normalize(correct)
            val distractors = distractorPool
                .filter { ArabicSpeechMatcher.normalize(it) != correctNorm }
                .shuffled(rng)
                .take(3)
            FillBlankQuestion(
                beforeWords = before,
                afterWords = after,
                correctWord = correct,
                options = (listOf(correct) + distractors).shuffled(rng)
            )
        }
    }

    val solvedSet = remember(hadith.id, seed) { mutableStateMapOf<Int, Boolean>() }
    val wrongPicks = remember(hadith.id, seed) { mutableStateMapOf<Int, String>() }

    ToyCard(
        containerColor = StorybookColors.SkySoft,
        borderColor = StorybookColors.SkyLight,
        lipColor = StorybookColors.SkyAccent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "لعبة أكمل الكلمة الناقصة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.SkyDark
                )
                Text(
                    text = "اختر الكلمة الصحيحة من الحديث لتُكمل الفراغ في كل جملة!",
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
            CandyButton(
                onClick = { seed++ },
                topColor = StorybookColors.SurfaceWhite,
                bottomColor = StorybookColors.GoldSoft,
                borderColor = StorybookColors.GoldMain,
                lipColor = StorybookColors.GoldLip,
                contentColor = StorybookColors.GoldDeep,
                cornerRadius = 13.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "جمل جديدة", tint = StorybookColors.GoldDeep, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            questions.forEachIndexed { qIdx, q ->
                val isSolved = solvedSet[qIdx] == true
                val lastWrong = wrongPicks[qIdx]

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = StorybookColors.SurfaceWhite,
                    border = BorderStroke(2.dp, if (isSolved) StorybookColors.EmeraldMain else StorybookColors.SkyLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "الجملة ${ArabicUtils.toArabicDigits(qIdx + 1)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.SkyDark
                        )

                        // Sentence preview with blank pill
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(StorybookColors.CreamBg)
                                .border(1.dp, StorybookColors.GoldBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            if (q.beforeWords.isNotBlank()) {
                                Text(
                                    text = "... ${q.beforeWords}",
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 21.sp,
                                    color = StorybookColors.StoryInk
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSolved) StorybookColors.EmeraldSoft else StorybookColors.GoldLight)
                                    .border(
                                        1.5.dp,
                                        if (isSolved) StorybookColors.EmeraldMain else StorybookColors.GoldDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isSolved) q.correctWord else "________",
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSolved) StorybookColors.EmeraldDark else StorybookColors.GoldDeep
                                )
                            }
                            if (q.afterWords.isNotBlank()) {
                                Text(
                                    text = "${q.afterWords} ...",
                                    fontFamily = AmiriFontFamily,
                                    fontSize = 21.sp,
                                    color = StorybookColors.StoryInk
                                )
                            }
                        }

                        // 4 Word Choices (2x2 grid)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            q.options.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { opt ->
                                        val isRight = isSolved && opt == q.correctWord
                                        val isWrong = !isSolved && lastWrong == opt
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = when {
                                                isRight -> StorybookColors.EmeraldMain
                                                isWrong -> StorybookColors.CoralSoft
                                                else -> StorybookColors.SkySoft
                                            },
                                            border = BorderStroke(
                                                1.5.dp,
                                                when {
                                                    isRight -> StorybookColors.EmeraldDark
                                                    isWrong -> StorybookColors.CoralMain
                                                    else -> StorybookColors.SkyLight
                                                }
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(enabled = !isSolved) {
                                                    if (opt == q.correctWord) {
                                                        solvedSet[qIdx] = true
                                                        wrongPicks.remove(qIdx)
                                                        onCorrectSfx()
                                                    } else {
                                                        wrongPicks[qIdx] = opt
                                                        onWrongSfx()
                                                    }
                                                }
                                        ) {
                                            Text(
                                                text = opt,
                                                fontFamily = AmiriFontFamily,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isRight -> Color.White
                                                    isWrong -> StorybookColors.CoralDeep
                                                    else -> StorybookColors.StoryNavy
                                                },
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = questions.isNotEmpty() && solvedSet.size == questions.size) {
            Column {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = StorybookColors.EmeraldSoft,
                    border = BorderStroke(2.dp, StorybookColors.EmeraldMain),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StorybookColors.GoldDark)
                        Text(
                            text = "ما شاء الله يا $heroName! أكملت جميع الفراغات ببراعة — اضغط التحديث لجمل جديدة!",
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

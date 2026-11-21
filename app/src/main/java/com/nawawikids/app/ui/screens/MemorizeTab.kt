package com.nawawikids.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nawawikids.app.audio.VoiceRecorderState
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.MemorizeMode
import com.nawawikids.app.speech.LiveRecitationState
import com.nawawikids.app.speech.RecitationStatus
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.CloudMatnCard
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.AmiriFontFamily
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.ui.viewmodel.StationUiState
import com.nawawikids.app.util.ArabicUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemorizeTab(
    hadith: Hadith,
    heroName: String,
    fontSize: FontSizeOption,
    stationState: StationUiState,
    recitationState: LiveRecitationState,
    recorderState: VoiceRecorderState,
    onSelectMode: (MemorizeMode) -> Unit,
    onStartLiveRecitation: () -> Unit,
    onStopLiveRecitation: () -> Unit,
    onRetryAfterMistake: () -> Unit,
    onHintCurrentWord: () -> Unit,
    onSkipCurrentWord: () -> Unit,
    onResetLiveRecitation: () -> Unit,
    onPlayWordOrPhraseHint: (Int, Int) -> Unit,
    onSetMemHidePercent: (Int) -> Unit,
    onReshuffleHidden: () -> Unit,
    onToggleFirstLetterHint: (Boolean) -> Unit,
    onTogglePeekWord: (Int) -> Unit,
    onRevealNextWord: () -> Unit,
    onRevealAllWords: () -> Unit,
    onStartSelfRecording: () -> Unit,
    onStopSelfRecording: () -> Unit,
    onToggleSelfPlayback: () -> Unit,
    onPlayQariCompare: () -> Unit,
    onGoToQuiz: () -> Unit
) {
    val context = LocalContext.current
    var pendingMicAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var micPermissionDenied by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micPermissionDenied = false
            pendingMicAction?.invoke()
            pendingMicAction = null
        } else {
            micPermissionDenied = true
            pendingMicAction = null
        }
    }

    fun runWithMicPermission(action: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            micPermissionDenied = false
            action()
        } else {
            pendingMicAction = action
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Mode Selector Strip: Live Voice Test vs Progressive Masking
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isLive = stationState.memorizeMode == MemorizeMode.LIVE_VOICE
            CandyButton(
                onClick = { onSelectMode(MemorizeMode.LIVE_VOICE) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_live_voice_button"),
                topColor = if (isLive) Color(0xFFA78BFA) else Color.White,
                bottomColor = if (isLive) StorybookColors.VioletMain else StorybookColors.VioletSoft,
                borderColor = if (isLive) StorybookColors.VioletDark else StorybookColors.VioletBorder,
                lipColor = if (isLive) StorybookColors.VioletDeep else StorybookColors.VioletMain,
                contentColor = if (isLive) Color.White else StorybookColors.VioletDeep,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 11.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = null,
                    tint = if (isLive) Color.White else StorybookColors.VioletDark,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "التسميع المباشر بالصوت",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isLive) Color.White else StorybookColors.VioletDeep
                )
            }

            val isHide = stationState.memorizeMode == MemorizeMode.PROGRESSIVE_HIDE
            CandyButton(
                onClick = { onSelectMode(MemorizeMode.PROGRESSIVE_HIDE) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_progressive_hide_button"),
                topColor = if (isHide) Color(0xFFFDE047) else Color.White,
                bottomColor = if (isHide) StorybookColors.GoldMain else StorybookColors.GoldSoft,
                borderColor = if (isHide) StorybookColors.GoldDark else StorybookColors.GoldBorder,
                lipColor = if (isHide) StorybookColors.GoldDeep else StorybookColors.GoldMain,
                contentColor = StorybookColors.GoldDeep,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 11.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = StorybookColors.GoldDeep,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "الإخفاء التدريجي والتسجيل",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.GoldDeep
                )
            }
        }

        // Mic Permission Rationale Banner (if denied)
        AnimatedVisibility(visible = micPermissionDenied) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = StorybookColors.CoralSoft,
                border = BorderStroke(2.dp, StorybookColors.CoralMain),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = StorybookColors.CoralDark)
                    Text(
                        text = "نحتاج إذن الميكروفون حتى يسمع البرنامج صوتك ويظهر الكلمات واحدةً بواحدة!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = StorybookColors.CoralDeep,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (stationState.memorizeMode == MemorizeMode.LIVE_VOICE) {
            LiveVoiceRecitationSection(
                hadith = hadith,
                heroName = heroName,
                fontSize = fontSize,
                recitationState = recitationState,
                onStartListening = { runWithMicPermission(onStartLiveRecitation) },
                onStopListening = onStopLiveRecitation,
                onRetryAfterMistake = { runWithMicPermission(onRetryAfterMistake) },
                onHintCurrentWord = onHintCurrentWord,
                onSkipCurrentWord = onSkipCurrentWord,
                onResetRecitation = onResetLiveRecitation,
                onPlayWordOrPhraseHint = onPlayWordOrPhraseHint
            )
        } else {
            ProgressiveMaskingSection(
                hadith = hadith,
                fontSize = fontSize,
                stationState = stationState,
                recorderState = recorderState,
                onSetMemHidePercent = onSetMemHidePercent,
                onReshuffleHidden = onReshuffleHidden,
                onToggleFirstLetterHint = onToggleFirstLetterHint,
                onTogglePeekWord = onTogglePeekWord,
                onRevealNextWord = onRevealNextWord,
                onRevealAllWords = onRevealAllWords,
                onStartSelfRecording = { runWithMicPermission(onStartSelfRecording) },
                onStopSelfRecording = onStopSelfRecording,
                onToggleSelfPlayback = onToggleSelfPlayback,
                onPlayQariCompare = onPlayQariCompare
            )
        }

        // Next Step CTA: Quiz & Games
        CandyButton(
            onClick = onGoToQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("goto_quiz_button"),
            topColor = Color(0xFFFB7185),
            bottomColor = Color(0xFFE11D48),
            borderColor = StorybookColors.CoralDark,
            lipColor = StorybookColors.CoralDeep
        ) {
            Text(
                text = "الخطوة الأخيرة: العب واربح النجمة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = StorybookColors.GoldLight,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LiveVoiceRecitationSection(
    hadith: Hadith,
    heroName: String,
    fontSize: FontSizeOption,
    recitationState: LiveRecitationState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onRetryAfterMistake: () -> Unit,
    onHintCurrentWord: () -> Unit,
    onSkipCurrentWord: () -> Unit,
    onResetRecitation: () -> Unit,
    onPlayWordOrPhraseHint: (Int, Int) -> Unit
) {
    val isListening = recitationState.status == RecitationStatus.LISTENING
    val isMistake = recitationState.status == RecitationStatus.MISTAKE_PAUSED
    val isCompleted = recitationState.status == RecitationStatus.COMPLETED

    val infinite = rememberInfiniteTransition(label = "micPulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "micScale"
    )

    val groupedParagraphs = remember(hadith.id) {
        hadith.words
            .withIndex()
            .groupBy { it.value.p }
            .toSortedMap()
    }

    // 1. Live Voice Studio Control Card
    ToyCard(
        containerColor = Color(0xFFFAF5FF),
        borderColor = StorybookColors.VioletBorder,
        lipColor = StorybookColors.VioletMain
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated Pulsing Mic Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .scale(pulse)
                    .background(
                        if (isListening) StorybookColors.CoralDeep else StorybookColors.VioletDeep,
                        CircleShape
                    )
                    .padding(bottom = 4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            if (isListening) listOf(Color(0xFFFB7185), StorybookColors.CoralDark)
                            else listOf(Color(0xFFA78BFA), StorybookColors.VioletDark)
                        ),
                        CircleShape
                    )
                    .border(3.dp, Color.White, CircleShape)
                    .clickable {
                        if (isListening) onStopListening() else onStartListening()
                    }
                    .testTag("live_recite_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    contentDescription = if (isListening) "إيقاف التسميع" else "ابدأ التسميع الصوتي",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = when {
                        isCompleted -> "ممتاز يا $heroName! أتممت التسميع!"
                        isListening -> "أستمع إليك الآن... واصل التلاوة!"
                        isMistake -> "توقّفنا لتصحيح كلمة!"
                        else -> "اختبر حفظك بالتسميع المباشر"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.VioletDeep
                )
                Text(
                    text = "اضغط الميكروفون واقرأ الحديث من حفظك — كل كلمة صحيحة ستظهر فوراً، ولو أخطأت سأوقفك لتعيدها!",
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Live Progress & Accuracy Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, StorybookColors.VioletBorder, CircleShape)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(recitationState.progressFraction.coerceAtLeast(if (recitationState.revealedIndices.isNotEmpty()) 0.04f else 0f))
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(listOf(StorybookColors.VioletMain, StorybookColors.EmeraldMain))
                        )
                )
            }
            Text(
                text = "${ArabicUtils.toArabicDigits(recitationState.revealedIndices.size)} / ${ArabicUtils.toArabicDigits(recitationState.totalWords)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = StorybookColors.VioletDeep
            )
            Surface(
                shape = CircleShape,
                color = StorybookColors.EmeraldSoft,
                border = BorderStroke(1.dp, StorybookColors.EmeraldBorder)
            ) {
                Text(
                    text = "دقة ${ArabicUtils.toArabicDigits(recitationState.accuracyPercent)}٪",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.EmeraldDark,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Live Audio Level Equalizer while listening
        AnimatedVisibility(visible = isListening) {
            Column {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(StorybookColors.CoralSoft)
                        .border(1.5.dp, StorybookColors.CoralBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = StorybookColors.CoralDark,
                        modifier = Modifier.size(22.dp)
                    )
                    // 5 reactive equalizer bars driven by RMS level
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val multipliers = listOf(0.55f, 1.0f, 0.75f, 0.92f, 0.60f)
                        multipliers.forEach { m ->
                            val h = (10f + 20f * recitationState.rmsLevel * m).dp
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(h)
                                    .clip(CircleShape)
                                    .background(StorybookColors.CoralMain)
                            )
                        }
                    }
                    Text(
                        text = if (recitationState.lastHeardSnippet.isNotBlank()) {
                            "سمعت: «${recitationState.lastHeardSnippet.takeLast(32)}»"
                        } else {
                            "تكلّم بوضوح يا بطل..."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StorybookColors.CoralDeep,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Helper Controls Row: Listen Next Phrase + Hint + Reset
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CandyButton(
                onClick = { onPlayWordOrPhraseHint(recitationState.currentTargetIndex, 4) },
                enabled = !isCompleted,
                modifier = Modifier.testTag("listen_phrase_helper_button"),
                topColor = Color.White,
                bottomColor = StorybookColors.TealSoft,
                borderColor = StorybookColors.TealMain,
                lipColor = StorybookColors.TealDark,
                contentColor = StorybookColors.TealDark,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
            ) {
                Icon(Icons.Rounded.Hearing, contentDescription = null, tint = StorybookColors.TealDark, modifier = Modifier.size(16.dp))
                Text("اسمع الجملة وكرّر", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StorybookColors.TealDark)
            }

            CandyButton(
                onClick = onHintCurrentWord,
                enabled = !isCompleted,
                modifier = Modifier.testTag("hint_current_word_button"),
                topColor = Color.White,
                bottomColor = StorybookColors.GoldSoft,
                borderColor = StorybookColors.GoldMain,
                lipColor = StorybookColors.GoldDark,
                contentColor = StorybookColors.GoldDeep,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
            ) {
                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = StorybookColors.GoldDark, modifier = Modifier.size(16.dp))
                Text("تلميح أول حرف", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StorybookColors.GoldDeep)
            }

            CandyButton(
                onClick = onResetRecitation,
                modifier = Modifier.testTag("reset_recitation_button"),
                topColor = Color.White,
                bottomColor = StorybookColors.SkySoft,
                borderColor = StorybookColors.SkyAccent,
                lipColor = StorybookColors.SkyDark,
                contentColor = StorybookColors.SkyDark,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, tint = StorybookColors.SkyDark, modifier = Modifier.size(16.dp))
                Text("إعادة من الأول", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StorybookColors.SkyDark)
            }
        }

        // Error message if SpeechRecognizer reported an issue
        AnimatedVisibility(visible = !recitationState.errorMessage.isNullOrBlank()) {
            Column {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StorybookColors.GoldSoft,
                    border = BorderStroke(1.5.dp, StorybookColors.GoldMain),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = recitationState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = StorybookColors.GoldDeep,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }

    // 2. Automatic Mistake Detection & Gentle Retry Card!
    AnimatedVisibility(visible = isMistake) {
        val targetIdx = recitationState.mistakeTargetIndex ?: recitationState.currentTargetIndex
        ToyCard(
            containerColor = StorybookColors.CoralSoft,
            borderColor = StorybookColors.CoralMain,
            lipColor = StorybookColors.CoralDark
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AssetImage(
                    assetPath = "img/boy_guide.png",
                    contentDescription = "عمر يساعدك",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, StorybookColors.CoralMain, CircleShape)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تنبيه لطيف يا $heroName! يوجد خطأ في الكلمة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.CoralDeep
                    )
                    Text(
                        text = "سمعتك تقول «${recitationState.mistakeWordSpoken.orEmpty()}» — استمع للكلمة الصحيحة أو حاول إعادتها الآن!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = StorybookColors.StoryInk
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CandyButton(
                    onClick = onRetryAfterMistake,
                    modifier = Modifier.testTag("retry_mistake_button"),
                    topColor = Color(0xFF34D399),
                    bottomColor = Color(0xFF059669),
                    borderColor = Color(0xFF047857),
                    lipColor = Color(0xFF064E3B),
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("أعد المحاولة الآن", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color.White)
                }

                CandyButton(
                    onClick = { onPlayWordOrPhraseHint(targetIdx, 1) },
                    modifier = Modifier.testTag("hear_missed_word_button"),
                    topColor = StorybookColors.GoldLight,
                    bottomColor = StorybookColors.GoldMain,
                    borderColor = StorybookColors.GoldDark,
                    lipColor = StorybookColors.GoldDeep,
                    contentColor = StorybookColors.GoldDeep,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, tint = StorybookColors.GoldDeep, modifier = Modifier.size(16.dp))
                    Text("اسمع الكلمة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.GoldDeep)
                }

                CandyButton(
                    onClick = onHintCurrentWord,
                    modifier = Modifier.testTag("mistake_hint_letter_button"),
                    topColor = Color.White,
                    bottomColor = StorybookColors.SkySoft,
                    borderColor = StorybookColors.SkyAccent,
                    lipColor = StorybookColors.SkyDark,
                    contentColor = StorybookColors.SkyDark,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = StorybookColors.SkyDark, modifier = Modifier.size(16.dp))
                    Text("أول حرف", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.SkyDark)
                }

                CandyButton(
                    onClick = onSkipCurrentWord,
                    modifier = Modifier.testTag("skip_reveal_word_button"),
                    topColor = Color.White,
                    bottomColor = StorybookColors.VioletSoft,
                    borderColor = StorybookColors.VioletBorder,
                    lipColor = StorybookColors.VioletDark,
                    contentColor = StorybookColors.VioletDeep,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = StorybookColors.VioletDark, modifier = Modifier.size(16.dp))
                    Text("أظهرها وأكمل", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.VioletDeep)
                }
            }
        }
    }

    // 3. Completion Celebration Banner!
    AnimatedVisibility(visible = isCompleted) {
        ToyCard(
            containerColor = StorybookColors.EmeraldSoft,
            borderColor = StorybookColors.EmeraldMain,
            lipColor = StorybookColors.EmeraldDark
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = StorybookColors.GoldMain,
                    modifier = Modifier.size(42.dp)
                )
                Text(
                    text = "ما شاء الله عليك يا $heroName! أتقنت تسميع الحديث بالكامل!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.EmeraldDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "دقة التسميع: ${ArabicUtils.toArabicDigits(recitationState.accuracyPercent)}٪ — حصلت على وسام الميكروفون لهذا الحديث!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = StorybookColors.StoryInk,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // 4. Scalloped Cloud Word-by-Word Live Reveal Box!
    CloudMatnCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "سحابة التسميع الحي — الكلمات تظهر واحدةً تلو الأخرى",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = StorybookColors.VioletDeep
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFFFDE68A))
        )
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            groupedParagraphs.forEach { (_, indexedWords) ->
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    indexedWords.forEach { (idx, word) ->
                        val isRevealed = recitationState.revealedIndices.contains(idx)
                        val isTarget = idx == recitationState.currentTargetIndex && !isCompleted
                        val isWrongTarget = isTarget && isMistake
                        val isHinted = recitationState.hintedIndices.contains(idx)

                        when {
                            isRevealed -> {
                                // Word successfully spoken & revealed! Tap to hear Qari pronounce it
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(StorybookColors.EmeraldSoft)
                                        .border(1.5.dp, StorybookColors.EmeraldBorder, RoundedCornerShape(11.dp))
                                        .clickable { onPlayWordOrPhraseHint(idx, 1) }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = word.t,
                                        fontFamily = AmiriFontFamily,
                                        fontSize = fontSize.spSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StorybookColors.EmeraldDark
                                    )
                                }
                            }

                            isTarget -> {
                                // Current word waiting to be spoken (pulsing target or coral red on mistake!)
                                val targetBg = if (isWrongTarget) StorybookColors.CoralSoft else StorybookColors.VioletSoft
                                val targetBorder = if (isWrongTarget) StorybookColors.CoralMain else StorybookColors.VioletMain
                                val targetInk = if (isWrongTarget) StorybookColors.CoralDeep else StorybookColors.VioletDeep
                                val displayTxt = if (isHinted) ArabicUtils.firstLetterHint(word.t) else if (isWrongTarget) "أعد الكلمة!" else "؟ ؟ ؟"

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(targetBg)
                                        .border(2.5.dp, targetBorder, RoundedCornerShape(12.dp))
                                        .clickable { onPlayWordOrPhraseHint(idx, 1) }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = displayTxt,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = targetInk
                                    )
                                }
                            }

                            else -> {
                                // Upcoming veiled mystery golden card
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(StorybookColors.GoldSoft)
                                        .border(1.5.dp, StorybookColors.GoldBorder, RoundedCornerShape(11.dp))
                                        .padding(horizontal = 11.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "••••",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = StorybookColors.GoldDark.copy(alpha = 0.7f)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgressiveMaskingSection(
    hadith: Hadith,
    fontSize: FontSizeOption,
    stationState: StationUiState,
    recorderState: VoiceRecorderState,
    onSetMemHidePercent: (Int) -> Unit,
    onReshuffleHidden: () -> Unit,
    onToggleFirstLetterHint: (Boolean) -> Unit,
    onTogglePeekWord: (Int) -> Unit,
    onRevealNextWord: () -> Unit,
    onRevealAllWords: () -> Unit,
    onStartSelfRecording: () -> Unit,
    onStopSelfRecording: () -> Unit,
    onToggleSelfPlayback: () -> Unit,
    onPlayQariCompare: () -> Unit
) {
    val groupedParagraphs = remember(hadith.id) {
        hadith.words
            .withIndex()
            .groupBy { it.value.p }
            .toSortedMap()
    }

    // 1. Progressive Hide Controls Card
    ToyCard {
        Text(
            text = "تحدّي الإخفاء التدريجي للبطاقات الصفراء",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = StorybookColors.VioletDeep
        )
        Text(
            text = "اختر نسبة الإخفاء، واقرأ من حفظك، ثم اضغط على أي بطاقة صفراء للتأكد من إجابتك!",
            style = MaterialTheme.typography.bodySmall,
            color = StorybookColors.StoryMuted
        )

        Spacer(Modifier.height(10.dp))

        val levels = listOf(
            0 to "١. ظاهر ٠٪",
            35 to "٢. تحدّي ٣٥٪",
            70 to "٣. تحدّي ٧٠٪",
            100 to "٤. كامل ١٠٠٪"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            levels.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { (pct, label) ->
                        val active = stationState.memHidePercent == pct
                        CandyButton(
                            onClick = { onSetMemHidePercent(pct) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mem_level_$pct"),
                            topColor = if (active) Color(0xFFA78BFA) else StorybookColors.VioletSoft,
                            bottomColor = if (active) StorybookColors.VioletMain else Color(0xFFEDE9FE),
                            borderColor = if (active) StorybookColors.VioletDark else StorybookColors.VioletBorder,
                            lipColor = if (active) StorybookColors.VioletDeep else StorybookColors.VioletMain,
                            contentColor = if (active) Color.White else StorybookColors.VioletDeep,
                            cornerRadius = 15.dp,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (active) Color.White else StorybookColors.VioletDeep
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = StorybookColors.VioletSoft,
                border = BorderStroke(1.5.dp, StorybookColors.VioletBorder),
                modifier = Modifier.clickable { onToggleFirstLetterHint(!stationState.firstLetterHint) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Checkbox(
                        checked = stationState.firstLetterHint,
                        onCheckedChange = onToggleFirstLetterHint,
                        colors = CheckboxDefaults.colors(checkedColor = StorybookColors.VioletMain)
                    )
                    Text(
                        text = "أول حرف",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.VioletDeep
                    )
                }
            }

            CandyButton(
                onClick = onRevealNextWord,
                topColor = StorybookColors.SkySoft,
                bottomColor = Color(0xFFBAE6FD),
                borderColor = Color(0xFF7DD3FC),
                lipColor = StorybookColors.SkyDark,
                contentColor = StorybookColors.SkyDark,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp)
            ) {
                Text("اكشف كلمة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.SkyDark)
            }

            CandyButton(
                onClick = onReshuffleHidden,
                topColor = StorybookColors.GoldSoft,
                bottomColor = Color(0xFFFDE68A),
                borderColor = Color(0xFFFCD34D),
                lipColor = StorybookColors.GoldDark,
                contentColor = StorybookColors.GoldDeep,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = StorybookColors.GoldDeep, modifier = Modifier.size(16.dp))
                Text("غيّر المخفي", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.GoldDeep)
            }

            CandyButton(
                onClick = onRevealAllWords,
                topColor = StorybookColors.GoldLight,
                bottomColor = Color(0xFFFACC15),
                borderColor = Color(0xFFEAB308),
                lipColor = Color(0xFFA16207),
                contentColor = StorybookColors.GoldDeep,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Visibility, contentDescription = null, tint = StorybookColors.GoldDeep, modifier = Modifier.size(16.dp))
                Text("اكشف الكل", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = StorybookColors.GoldDeep)
            }
        }
    }

    // 2. Scalloped Cloud Box for Progressive Masking
    CloudMatnCard {
        val remainingHidden = (stationState.memHiddenIndices - stationState.memPeekedIndices).size
        Text(
            text = if (stationState.memHiddenIndices.isEmpty()) {
                "النص ظاهر بالكامل للقراءة والمراجعة"
            } else {
                "مخفي ${ArabicUtils.toArabicDigits(remainingHidden)} كلمة — اضغط على أي بطاقة صفراء لكشفها"
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = StorybookColors.VioletDark
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            groupedParagraphs.forEach { (_, indexedWords) ->
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    indexedWords.forEach { (idx, word) ->
                        val isMasked = stationState.memHiddenIndices.contains(idx)
                        val isPeeked = stationState.memPeekedIndices.contains(idx)
                        when {
                            !isMasked -> {
                                Text(
                                    text = word.t,
                                    fontFamily = AmiriFontFamily,
                                    fontSize = fontSize.spSize.sp,
                                    color = StorybookColors.StoryInk,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
                                )
                            }
                            isPeeked -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(StorybookColors.EmeraldSoft)
                                        .border(2.dp, StorybookColors.EmeraldMain, RoundedCornerShape(11.dp))
                                        .clickable { onTogglePeekWord(idx) }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = word.t,
                                        fontFamily = AmiriFontFamily,
                                        fontSize = fontSize.spSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StorybookColors.EmeraldDark
                                    )
                                }
                            }
                            else -> {
                                val hintTxt = if (stationState.firstLetterHint) ArabicUtils.firstLetterHint(word.t) else "....."
                                Box(
                                    modifier = Modifier
                                        .background(StorybookColors.GoldDark, RoundedCornerShape(12.dp))
                                        .padding(bottom = 3.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.verticalGradient(listOf(StorybookColors.GoldSoft, Color(0xFFFDE68A))))
                                        .border(2.dp, StorybookColors.GoldDark, RoundedCornerShape(12.dp))
                                        .clickable { onTogglePeekWord(idx) }
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = hintTxt,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = StorybookColors.GoldDeep
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Self-Recitation Recording & Qari Comparison Studio
    ToyCard(
        containerColor = Color(0xFFFFF5F7),
        borderColor = StorybookColors.CoralBorder,
        lipColor = StorybookColors.CoralMain
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AssetImage(
                assetPath = "img/girl_guide.png",
                contentDescription = null,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "استوديو تسجيل صوتك ومقارنته بالقارئ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.CoralDeep
                )
                Text(
                    text = "سجّل تسميعك الكامل ثم اسمعه وقارنه بصوت القارئ!",
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (!recorderState.isRecording) {
            CandyButton(
                onClick = onStartSelfRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_self_record_button"),
                topColor = Color(0xFFFB7185),
                bottomColor = StorybookColors.CoralMain,
                borderColor = StorybookColors.CoralDark,
                lipColor = StorybookColors.CoralDeep
            ) {
                Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("سجّل تسميعي الآن", fontWeight = FontWeight.Black, color = Color.White)
            }
        } else {
            CandyButton(
                onClick = onStopSelfRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stop_self_record_button"),
                topColor = Color(0xFFB91C1C),
                bottomColor = Color(0xFF991B1B),
                borderColor = Color(0xFF7F1D1D),
                lipColor = Color(0xFF450A0A)
            ) {
                Icon(Icons.Rounded.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text(
                    text = "أنهِ التسجيل وراجع (${ArabicUtils.formatClock(recorderState.elapsedSeconds.toDouble())})",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        AnimatedVisibility(visible = recorderState.hasRecording) {
            Column {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StorybookColors.TealSoft,
                    border = BorderStroke(2.dp, StorybookColors.TealBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = StorybookColors.TealDark, modifier = Modifier.size(18.dp))
                            Text(
                                text = "أحسنت! اسمع تسجيلك الآن وقارنه بالقارئ:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.TealDark
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CandyButton(
                                onClick = onToggleSelfPlayback,
                                modifier = Modifier.weight(1f),
                                topColor = Color.White,
                                bottomColor = StorybookColors.GoldSoft,
                                borderColor = StorybookColors.GoldMain,
                                lipColor = StorybookColors.GoldDark,
                                contentColor = StorybookColors.GoldDeep,
                                cornerRadius = 14.dp,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (recorderState.isPlayingBack) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = StorybookColors.GoldDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (recorderState.isPlayingBack) "إيقاف صوتي" else "اسمع صوتي",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = StorybookColors.GoldDeep
                                )
                            }
                            CandyButton(
                                onClick = onPlayQariCompare,
                                modifier = Modifier.weight(1f),
                                topColor = StorybookColors.TealMain,
                                bottomColor = StorybookColors.TealDark,
                                borderColor = StorybookColors.TealDark,
                                lipColor = Color(0xFF115E59),
                                cornerRadius = 14.dp,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Text("صوت القارئ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

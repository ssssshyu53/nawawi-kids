package com.nawawikids.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nawawikids.app.audio.AudioPlaybackState
import com.nawawikids.app.audio.VoiceRecorderState
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.HadithProgressRecord
import com.nawawikids.app.model.MemorizeMode
import com.nawawikids.app.model.QuizMode
import com.nawawikids.app.model.StationTab
import com.nawawikids.app.model.StorybookChapters
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.speech.LiveRecitationState
import com.nawawikids.app.speech.RecitationStatus
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.GoldenStarMedallion
import com.nawawikids.app.ui.components.RibbonBadge
import com.nawawikids.app.ui.components.StorybookBackground
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.ui.viewmodel.StationUiState
import com.nawawikids.app.util.ArabicUtils

@Composable
fun StationScreen(
    hadith: Hadith,
    allHadiths: List<Hadith>,
    progress: UserProgress,
    stationState: StationUiState,
    audioState: AudioPlaybackState,
    recitationState: LiveRecitationState,
    recorderState: VoiceRecorderState,
    onBackToMap: () -> Unit,
    onNavigateHadith: (Int) -> Unit,
    onSelectTab: (StationTab) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayQariFromStart: () -> Unit,
    onSeekAudio: (Double, Boolean) -> Unit,
    onRewind5s: () -> Unit,
    onForward5s: () -> Unit,
    onCycleSpeed: () -> Unit,
    onCycleLoop: () -> Unit,
    onSelectFontSize: (FontSizeOption) -> Unit,
    onSelectMemorizeMode: (MemorizeMode) -> Unit,
    onStartLiveRecitation: () -> Unit,
    onStopLiveRecitation: () -> Unit,
    onRetryAfterMistake: () -> Unit,
    onHintCurrentWord: () -> Unit,
    onSkipCurrentWord: () -> Unit,
    onResetLiveRecitation: () -> Unit,
    onSetPauseOnMistake: (Boolean) -> Unit,
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
    onSelectQuizMode: (QuizMode) -> Unit,
    onSubmitQuizAnswer: (Int, Int) -> Unit,
    onWordPuzzleSolved: () -> Unit,
    onVocabMatchSolved: () -> Unit,
    onGameCorrectSfx: () -> Unit,
    onGameWrongSfx: () -> Unit,
    onOpenCertificate: () -> Unit
) {
    var showPosterModal by remember { mutableStateOf(false) }
    var audioDrawerExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(stationState.activeTab) {
        audioDrawerExpanded = false
    }
    val chapter = StorybookChapters.byId(hadith.stage)
    val total = allHadiths.size.coerceAtLeast(42)
    val canPrev = hadith.id > 1 && progress.isUnlocked(hadith.id - 1)
    val canNext = hadith.id < total && progress.isUnlocked(hadith.id + 1)
    val isCompleted = progress.completedIds.contains(hadith.id)
    val record = progress.recordFor(hadith.id)

    val statusPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    StorybookBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusPadding)
        ) {
            // Top Navigation Bar
            Surface(
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CandyButton(
                        onClick = onBackToMap,
                        modifier = Modifier.testTag("back_to_map_button"),
                        topColor = Color(0xFF38BDF8),
                        bottomColor = Color(0xFF0284C7),
                        borderColor = StorybookColors.SkyDark,
                        lipColor = Color(0xFF075985),
                        cornerRadius = 15.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = "خريطة الرحلة",
                            tint = StorybookColors.GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("الخريطة", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    Surface(
                        shape = CircleShape,
                        color = chapter.bg,
                        border = BorderStroke(1.5.dp, chapter.border)
                    ) {
                        Text(
                            text = "${chapter.name} • ${ArabicUtils.toArabicDigits(hadith.id)}/${ArabicUtils.toArabicDigits(total)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = chapter.ink,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CandyButton(
                            onClick = { onNavigateHadith(hadith.id - 1) },
                            enabled = canPrev,
                            modifier = Modifier.testTag("prev_hadith_button"),
                            topColor = StorybookColors.GoldSoft,
                            bottomColor = Color(0xFFFDE68A),
                            borderColor = StorybookColors.GoldMain,
                            lipColor = StorybookColors.GoldDark,
                            contentColor = StorybookColors.GoldDeep,
                            cornerRadius = 14.dp,
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "السابق",
                                tint = StorybookColors.GoldDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        CandyButton(
                            onClick = { onNavigateHadith(hadith.id + 1) },
                            enabled = canNext,
                            modifier = Modifier.testTag("next_hadith_button"),
                            topColor = StorybookColors.TealSoft,
                            bottomColor = Color(0xFF99F6E4),
                            borderColor = StorybookColors.TealMain,
                            lipColor = StorybookColors.TealDark,
                            contentColor = StorybookColors.TealDark,
                            cornerRadius = 14.dp,
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "التالي",
                                tint = StorybookColors.TealDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Scrollable Station Workspace + True Floating Audio Capsule Overlay (no background strip behind it!)
            val showFloatingAudio = recitationState.status != RecitationStatus.LISTENING && !recorderState.isRecording
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .widthIn(max = 760.dp)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                // Compact Station Title + Companion Tip Banner
                CompactStationBanner(
                    hadith = hadith,
                    heroName = progress.heroDisplayName,
                    avatarAsset = progress.avatarChoice.assetPath,
                    isCompleted = isCompleted,
                    record = record,
                    isStarPulsing = audioState.isHeaderActive,
                    activeTab = stationState.activeTab,
                    onOpenPoster = { showPosterModal = true }
                )

                // Active Step Content
                when (stationState.activeTab) {
                    StationTab.LISTEN -> ListenTab(
                        hadith = hadith,
                        audioState = audioState,
                        fontSize = progress.fontSize,
                        onSeekAudio = onSeekAudio,
                        onSelectFontSize = onSelectFontSize,
                        onOpenPoster = { showPosterModal = true },
                        onGoToLearn = { onSelectTab(StationTab.LEARN) }
                    )

                    StationTab.LEARN -> LearnTab(
                        hadith = hadith,
                        heroName = progress.heroDisplayName,
                        avatarAsset = progress.avatarChoice.assetPath,
                        onOpenPoster = { showPosterModal = true },
                        onCheckLessonSfx = onGameCorrectSfx,
                        onGoToMemorize = { onSelectTab(StationTab.MEMORIZE) }
                    )

                    StationTab.MEMORIZE -> MemorizeTab(
                        hadith = hadith,
                        heroName = progress.heroDisplayName,
                        fontSize = progress.fontSize,
                        stationState = stationState,
                        recitationState = recitationState,
                        recorderState = recorderState,
                        onSelectMode = onSelectMemorizeMode,
                        onStartLiveRecitation = onStartLiveRecitation,
                        onStopLiveRecitation = onStopLiveRecitation,
                        onRetryAfterMistake = onRetryAfterMistake,
                        onHintCurrentWord = onHintCurrentWord,
                        onSkipCurrentWord = onSkipCurrentWord,
                        onResetLiveRecitation = onResetLiveRecitation,
                        onSetPauseOnMistake = onSetPauseOnMistake,
                        onPlayWordOrPhraseHint = onPlayWordOrPhraseHint,
                        onSetMemHidePercent = onSetMemHidePercent,
                        onReshuffleHidden = onReshuffleHidden,
                        onToggleFirstLetterHint = onToggleFirstLetterHint,
                        onTogglePeekWord = onTogglePeekWord,
                        onRevealNextWord = onRevealNextWord,
                        onRevealAllWords = onRevealAllWords,
                        onStartSelfRecording = onStartSelfRecording,
                        onStopSelfRecording = onStopSelfRecording,
                        onToggleSelfPlayback = onToggleSelfPlayback,
                        onPlayQariCompare = onPlayQariFromStart,
                        onGoToQuiz = { onSelectTab(StationTab.QUIZ) }
                    )

                    StationTab.QUIZ -> QuizTab(
                        hadith = hadith,
                        allHadiths = allHadiths,
                        progress = progress,
                        stationState = stationState,
                        onSelectQuizMode = onSelectQuizMode,
                        onSubmitAnswer = onSubmitQuizAnswer,
                        onWordPuzzleSolved = onWordPuzzleSolved,
                        onVocabMatchSolved = onVocabMatchSolved,
                        onGameCorrectSfx = onGameCorrectSfx,
                        onGameWrongSfx = onGameWrongSfx,
                        onNextStation = { if (hadith.id < total) onNavigateHadith(hadith.id + 1) },
                        onBackToMap = onBackToMap,
                        onOpenCertificate = onOpenCertificate
                    )
                }

                    // Extra bottom breathing room so the last card scrolls cleanly above the floating capsule
                    Spacer(Modifier.height(if (showFloatingAudio) 86.dp else 10.dp))
                }

                // True Floating Audio Capsule overlaid transparently over the page across ALL tabs
                androidx.compose.animation.AnimatedVisibility(
                    visible = showFloatingAudio,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    val activeCaptionWord = hadith.words.getOrNull(audioState.activeWordIndex)?.t
                        ?: hadith.authorNote.firstOrNull {
                            audioState.isPlaying &&
                                audioState.currentSec >= it.s &&
                                audioState.currentSec <= it.e + 0.12
                        }?.t
                    StickyHadithAudioBar(
                        hadithTitle = hadith.title,
                        audioState = audioState,
                        activeWordText = activeCaptionWord,
                        expanded = audioDrawerExpanded,
                        onToggleExpanded = { audioDrawerExpanded = !audioDrawerExpanded },
                        onTogglePlayPause = onTogglePlayPause,
                        onPlayFromStart = onPlayQariFromStart,
                        onSeekAudio = onSeekAudio,
                        onRewind5s = onRewind5s,
                        onForward5s = onForward5s,
                        onCycleSpeed = onCycleSpeed,
                        onCycleLoop = onCycleLoop
                    )
                }
            }

            // Persistent Thumb-Friendly Bottom 4-Step Navigation Bar (with safe navigationBarsPadding!)
            StationBottomStepDock(
                activeTab = stationState.activeTab,
                record = record,
                visitedTabs = stationState.visitedTabs,
                onSelectTab = onSelectTab
            )
        }
    }

    // Fullscreen Poster Lightbox Modal
    if (showPosterModal && !hadith.poster.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { showPosterModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE60F172A))
                    .clickable { showPosterModal = false }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = StorybookColors.CreamBg,
                    border = BorderStroke(3.dp, StorybookColors.GoldMain),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${hadith.header} — ${hadith.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.StoryNavy,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showPosterModal = false },
                                modifier = Modifier.testTag("close_poster_modal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "إغلاق اللوحة",
                                    tint = StorybookColors.CoralDark
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        AssetImage(
                            assetPath = hadith.poster,
                            contentDescription = hadith.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactStationBanner(
    hadith: Hadith,
    heroName: String,
    avatarAsset: String,
    isCompleted: Boolean,
    record: HadithProgressRecord,
    isStarPulsing: Boolean,
    activeTab: StationTab,
    onOpenPoster: () -> Unit
) {
    ToyCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldenStarMedallion(
                number = hadith.id,
                pulsing = isStarPulsing,
                size = 64.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RibbonBadge(text = hadith.header)
                    if (isCompleted) {
                        Surface(
                            shape = CircleShape,
                            color = StorybookColors.EmeraldSoft,
                            border = BorderStroke(1.dp, StorybookColors.EmeraldBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = StorybookColors.EmeraldDark, modifier = Modifier.size(13.dp))
                                Text("نجمة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StorybookColors.EmeraldDark)
                            }
                        }
                    }
                    if (record.bestRecitationAccuracy > 0) {
                        Surface(
                            shape = CircleShape,
                            color = StorybookColors.VioletSoft,
                            border = BorderStroke(1.dp, StorybookColors.VioletBorder)
                        ) {
                            Text(
                                text = "تسميع ${ArabicUtils.toArabicDigits(record.bestRecitationAccuracy)}٪",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.VioletDark,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = hadith.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.StoryInk
                )
            }

            if (!hadith.poster.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(StorybookColors.GoldLight)
                        .border(2.dp, StorybookColors.GoldMain, RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenPoster)
                        .testTag("open_poster_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = "اللوحة المصورة",
                        tint = StorybookColors.GoldDeep,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        val tipMsg = when (activeTab) {
            StationTab.LISTEN -> "يا $heroName، اضغط على أي كلمة في السحابة لتسمع نطقها فوراً من القارئ!"
            StationTab.LEARN -> "يا $heroName، تعرّف على معاني الكلمات الغريبة والدروس الجميلة في الحديث."
            StationTab.MEMORIZE -> "يا $heroName، سمّع الحديث بصوتك لتظهر الكلمات واحدةً بواحدة، وسأصحح لك تلقائياً!"
            StationTab.QUIZ -> "يا $heroName، أجب على الأسئلة والعب ترتيب الكلمات وتوصيل المعاني لتربح النجمة!"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFF0F9FF))))
                .border(1.5.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            AssetImage(
                assetPath = avatarAsset,
                contentDescription = "صديق الرحلة",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Text(
                text = tipMsg,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = StorybookColors.StoryNavy,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StationBottomStepDock(
    activeTab: StationTab,
    record: HadithProgressRecord,
    visitedTabs: Set<StationTab>,
    onSelectTab: (StationTab) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, StorybookColors.GoldBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockStepButton(
                label = "استمع",
                icon = Icons.Rounded.Headphones,
                activeColor = StorybookColors.TealMain,
                darkColor = StorybookColors.TealDark,
                selected = activeTab == StationTab.LISTEN,
                done = record.listened || visitedTabs.contains(StationTab.LISTEN),
                onClick = { onSelectTab(StationTab.LISTEN) },
                modifier = Modifier.weight(1f).testTag("tab_listen")
            )
            DockStepButton(
                label = "افهم",
                icon = Icons.Rounded.Lightbulb,
                activeColor = StorybookColors.GoldMain,
                darkColor = StorybookColors.GoldDark,
                selected = activeTab == StationTab.LEARN,
                done = record.learned || visitedTabs.contains(StationTab.LEARN),
                onClick = { onSelectTab(StationTab.LEARN) },
                modifier = Modifier.weight(1f).testTag("tab_learn")
            )
            DockStepButton(
                label = "سمّع",
                icon = Icons.Rounded.Mic,
                activeColor = StorybookColors.VioletMain,
                darkColor = StorybookColors.VioletDark,
                selected = activeTab == StationTab.MEMORIZE,
                done = record.memorized,
                onClick = { onSelectTab(StationTab.MEMORIZE) },
                modifier = Modifier.weight(1f).testTag("tab_memorize")
            )
            DockStepButton(
                label = "العب",
                icon = Icons.Rounded.Star,
                activeColor = StorybookColors.CoralMain,
                darkColor = StorybookColors.CoralDark,
                selected = activeTab == StationTab.QUIZ,
                done = record.quizCompleted,
                onClick = { onSelectTab(StationTab.QUIZ) },
                modifier = Modifier.weight(1f).testTag("tab_quiz")
            )
        }
    }
}

@Composable
private fun DockStepButton(
    label: String,
    icon: ImageVector,
    activeColor: Color,
    darkColor: Color,
    selected: Boolean,
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) activeColor else activeColor.copy(alpha = 0.10f)
    val contentCol = if (selected) Color.White else darkColor

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) darkColor else activeColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentCol,
                    modifier = Modifier.size(20.dp)
                )
                if (done && !selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(StorybookColors.EmeraldDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = contentCol,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

package com.nawawikids.app.ui.screens

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nawawikids.app.audio.AudioPlaybackState
import com.nawawikids.app.audio.VoiceRecorderState
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.MemorizeMode
import com.nawawikids.app.model.QuizMode
import com.nawawikids.app.model.StationTab
import com.nawawikids.app.model.StorybookChapters
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.speech.LiveRecitationState
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
    val scrollState = rememberScrollState()
    val chapter = StorybookChapters.byId(hadith.stage)
    val total = allHadiths.size.coerceAtLeast(42)
    val canPrev = hadith.id > 1 && progress.isUnlocked(hadith.id - 1)
    val canNext = hadith.id < total && progress.isUnlocked(hadith.id + 1)
    val isCompleted = progress.completedIds.contains(hadith.id)
    val isRecited = progress.recitedIds.contains(hadith.id)

    val statusPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    StorybookBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusPadding)
        ) {
            // Top Navigation Bar
            Surface(
                color = Color.White.copy(alpha = 0.94f),
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
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
                        color = StorybookColors.GoldSoft,
                        border = BorderStroke(1.5.dp, Color(0xFFFCD34D))
                    ) {
                        Text(
                            text = "المحطة ${ArabicUtils.toArabicDigits(hadith.id)} من ${ArabicUtils.toArabicDigits(total)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.GoldDark,
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
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "المحطة السابقة",
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
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "المحطة التالية",
                                tint = StorybookColors.TealDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Scrollable Station Workspace
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .widthIn(max = 760.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 28.dp + navPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Station Title Banner + Companion Speech + 4-Step Visual Stepper
                StationHeaderCard(
                    hadith = hadith,
                    chapterName = "${chapter.ord}: ${chapter.name}",
                    heroName = progress.heroDisplayName,
                    isCompleted = isCompleted,
                    isRecited = isRecited,
                    isStarPulsing = audioState.isHeaderActive,
                    activeTab = stationState.activeTab,
                    visitedTabs = stationState.visitedTabs,
                    onSelectTab = onSelectTab,
                    onOpenPoster = { showPosterModal = true }
                )

                // 2. Active Step Content
                when (stationState.activeTab) {
                    StationTab.LISTEN -> ListenTab(
                        hadith = hadith,
                        audioState = audioState,
                        fontSize = progress.fontSize,
                        onTogglePlayPause = onTogglePlayPause,
                        onSeekAudio = onSeekAudio,
                        onRewind5s = onRewind5s,
                        onCycleSpeed = onCycleSpeed,
                        onCycleLoop = onCycleLoop,
                        onSelectFontSize = onSelectFontSize,
                        onOpenPoster = { showPosterModal = true },
                        onGoToLearn = { onSelectTab(StationTab.LEARN) }
                    )

                    StationTab.LEARN -> LearnTab(
                        hadith = hadith,
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
            }
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
private fun StationHeaderCard(
    hadith: Hadith,
    chapterName: String,
    heroName: String,
    isCompleted: Boolean,
    isRecited: Boolean,
    isStarPulsing: Boolean,
    activeTab: StationTab,
    visitedTabs: Set<StationTab>,
    onSelectTab: (StationTab) -> Unit,
    onOpenPoster: () -> Unit
) {
    ToyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldenStarMedallion(
                number = hadith.id,
                pulsing = isStarPulsing,
                size = 72.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
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
                            border = BorderStroke(1.5.dp, StorybookColors.EmeraldBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = StorybookColors.EmeraldDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "نجمة مكتسبة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = StorybookColors.EmeraldDark
                                )
                            }
                        }
                    }
                }
                Text(
                    text = hadith.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.StoryInk
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chapterName,
                        style = MaterialTheme.typography.labelSmall,
                        color = StorybookColors.SkyDark,
                        fontWeight = FontWeight.Bold
                    )
                    if (isRecited) {
                        Surface(
                            shape = CircleShape,
                            color = StorybookColors.VioletSoft,
                            border = BorderStroke(1.dp, StorybookColors.VioletBorder)
                        ) {
                            Text(
                                text = "أُتقن بالتسميع الصوتي",
                                style = MaterialTheme.typography.labelSmall,
                                color = StorybookColors.VioletDark,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        if (!hadith.poster.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            CandyButton(
                onClick = onOpenPoster,
                modifier = Modifier.testTag("open_poster_button"),
                topColor = StorybookColors.GoldLight,
                bottomColor = Color(0xFFFACC15),
                borderColor = Color(0xFFEAB308),
                lipColor = Color(0xFFA16207),
                contentColor = StorybookColors.GoldDeep,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = null,
                    tint = StorybookColors.GoldDeep,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "شاهد اللوحة المصورة الكاملة للحديث",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.GoldDeep
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Companion Cheer Speech Bubble
        val (guideImg, cheerMsg) = when (activeTab) {
            StationTab.LISTEN -> "img/boy_guide.png" to "الخطوة الأولى يا $heroName: هيا نستمع للحديث معاً! اضغط على أي كلمة في السحابة لتسمع نطقها فوراً."
            StationTab.LEARN -> "img/girl_guide.png" to "الخطوة الثانية: تعال يا $heroName نكتشف معاني الكلمات الجميلة والدروس المستفادة من هذا الحديث."
            StationTab.MEMORIZE -> "img/boy_guide.png" to "الخطوة الثالثة: اختبر حفظك يا $heroName! سمّع الحديث بصوتك لتظهر الكلمات واحدةً تلو الأخرى، وإذا أخطأت سأساعدك فوراً!"
            StationTab.QUIZ -> "img/girl_guide.png" to "الخطوة الأخيرة يا $heroName: أجب على الأسئلة والعب الألعاب التفاعلية لتربح النجمة الذهبية!"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFF0F9FF))))
                .border(2.dp, Color(0xFFBAE6FD), RoundedCornerShape(18.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AssetImage(
                assetPath = guideImg,
                contentDescription = "صديق الرحلة",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "صديقك في الرحلة يقول:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.SkyDark
                )
                Text(
                    text = cheerMsg,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = StorybookColors.StoryNavy
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFFF6E3B6))
        )
        Spacer(Modifier.height(12.dp))

        // 4 Connected Story Steps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            StepTabItem(
                tab = StationTab.LISTEN,
                stepNum = "١",
                label = "استمع",
                icon = Icons.Rounded.Headphones,
                mainColor = StorybookColors.TealMain,
                darkColor = StorybookColors.TealDark,
                active = activeTab == StationTab.LISTEN,
                done = isCompleted || visitedTabs.contains(StationTab.LISTEN),
                onClick = { onSelectTab(StationTab.LISTEN) },
                modifier = Modifier.weight(1f).testTag("tab_listen")
            )
            StepTabItem(
                tab = StationTab.LEARN,
                stepNum = "٢",
                label = "افهم",
                icon = Icons.Rounded.Lightbulb,
                mainColor = StorybookColors.GoldMain,
                darkColor = StorybookColors.GoldDark,
                active = activeTab == StationTab.LEARN,
                done = isCompleted || visitedTabs.contains(StationTab.LEARN),
                onClick = { onSelectTab(StationTab.LEARN) },
                modifier = Modifier.weight(1f).testTag("tab_learn")
            )
            StepTabItem(
                tab = StationTab.MEMORIZE,
                stepNum = "٣",
                label = "احفظ وسمّع",
                icon = Icons.Rounded.Mic,
                mainColor = StorybookColors.VioletMain,
                darkColor = StorybookColors.VioletDark,
                active = activeTab == StationTab.MEMORIZE,
                done = isCompleted || isRecited || visitedTabs.contains(StationTab.MEMORIZE),
                onClick = { onSelectTab(StationTab.MEMORIZE) },
                modifier = Modifier.weight(1f).testTag("tab_memorize")
            )
            StepTabItem(
                tab = StationTab.QUIZ,
                stepNum = "٤",
                label = "العب واربح",
                icon = Icons.Rounded.Star,
                mainColor = StorybookColors.CoralMain,
                darkColor = StorybookColors.CoralDark,
                active = activeTab == StationTab.QUIZ,
                done = isCompleted,
                onClick = { onSelectTab(StationTab.QUIZ) },
                modifier = Modifier.weight(1f).testTag("tab_quiz")
            )
        }
    }
}

@Composable
private fun StepTabItem(
    tab: StationTab,
    stepNum: String,
    label: String,
    icon: ImageVector,
    mainColor: Color,
    darkColor: Color,
    active: Boolean,
    done: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(if (active) 54.dp else 48.dp)
                    .background(darkColor, CircleShape)
                    .padding(bottom = 4.dp)
                    .clip(CircleShape)
                    .background(if (active) mainColor else Color.White, CircleShape)
                    .border(2.5.dp, if (active) Color.White else mainColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (active) Color.White else mainColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            // Step number badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (active) StorybookColors.GoldLight else darkColor)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNum,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (active) Color(0xFF5C2C06) else Color.White
                )
            }
            // Visited / Completed checkmark
            if (done && !active) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(19.dp)
                        .clip(CircleShape)
                        .background(StorybookColors.EmeraldDark)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = darkColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

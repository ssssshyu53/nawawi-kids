package com.nawawikids.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.StorybookBackground
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.notifications.DailyReminderScheduler
import com.nawawikids.app.ui.screens.CertificateDialog
import com.nawawikids.app.ui.screens.JourneyMapScreen
import com.nawawikids.app.ui.screens.OnboardingScreen
import com.nawawikids.app.ui.screens.ProgressDashboardScreen
import com.nawawikids.app.ui.screens.StationScreen
import com.nawawikids.app.ui.screens.StudentProfileDialog
import com.nawawikids.app.ui.screens.SunnahLibraryDialog
import com.nawawikids.app.ui.theme.NawawiKidsTheme
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.ui.viewmodel.DatasetLoadState
import com.nawawikids.app.ui.viewmodel.JourneyViewModel
import kotlinx.serialization.Serializable

@Serializable
data object JourneyRoute

@Serializable
data object DashboardRoute

@Serializable
data class StationRoute(val hadithId: Int)

class MainActivity : ComponentActivity() {
    private val pendingReminderHadithId = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractReminderTarget(intent)
        setContent {
            NawawiKidsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    NawawiKidsApp(
                        pendingHadithId = pendingReminderHadithId.value,
                        onConsumePendingHadith = { pendingReminderHadithId.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        extractReminderTarget(intent)
    }

    private fun extractReminderTarget(intent: Intent?) {
        val targetId = intent?.getIntExtra(DailyReminderScheduler.EXTRA_OPEN_HADITH_ID, -1) ?: -1
        if (targetId in 1..42) {
            pendingReminderHadithId.value = targetId
        }
    }
}

@Composable
private fun NawawiKidsApp(
    pendingHadithId: Int? = null,
    onConsumePendingHadith: () -> Unit = {},
    viewModel: JourneyViewModel = viewModel()
) {
    val datasetState by viewModel.datasetState.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val progressLoaded by viewModel.progressLoaded.collectAsStateWithLifecycle()
    val stationState by viewModel.station.collectAsStateWithLifecycle()
    val selectedWorldId by viewModel.selectedWorldId.collectAsStateWithLifecycle()
    val mapFilter by viewModel.mapFilter.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val recitationState by viewModel.recitationState.collectAsStateWithLifecycle()
    val recorderState by viewModel.recorderState.collectAsStateWithLifecycle()

    var showProfile by remember { mutableStateOf(false) }
    var showCertificate by remember { mutableStateOf(false) }
    var showLibrary by remember { mutableStateOf(false) }

    when (val ds = datasetState) {
        DatasetLoadState.Loading -> {
            StorybookBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ToyCard {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            AssetImage(
                                assetPath = "img/book_guide.png",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = "نفتح كتاب الأربعين النووية للأبطال...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.StoryInk,
                                textAlign = TextAlign.Center
                            )
                            CircularProgressIndicator(color = StorybookColors.TealMain)
                        }
                    }
                }
            }
        }

        is DatasetLoadState.Error -> {
            StorybookBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ToyCard {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "تعذر فتح الكتاب",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = StorybookColors.CoralDark
                            )
                            Text(
                                text = ds.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = StorybookColors.StoryMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            CandyButton(onClick = { viewModel.loadDataset() }) {
                                Text("أعد المحاولة", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        is DatasetLoadState.Ready -> {
            if (!progressLoaded) {
                StorybookBackground {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StorybookColors.TealMain)
                    }
                }
            } else if (!progress.hasSeenOnboarding) {
                OnboardingScreen(
                    initialName = progress.studentName,
                    initialAge = progress.studentAge,
                    initialAvatar = progress.avatarChoice,
                    onFinish = viewModel::completeOnboarding,
                    onSkip = viewModel::skipOnboarding
                )
            } else {
                val navController = rememberNavController()
                val dataset = ds.dataset

                LaunchedEffect(pendingHadithId) {
                    if (pendingHadithId != null) {
                        val safeId = pendingHadithId.coerceIn(1, dataset.hadiths.size.coerceAtLeast(1))
                        if (progress.isUnlocked(safeId)) {
                            viewModel.openStation(safeId)
                            navController.navigate(StationRoute(safeId)) {
                                launchSingleTop = true
                            }
                        }
                        onConsumePendingHadith()
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = JourneyRoute
                ) {
                composable<JourneyRoute> {
                    JourneyMapScreen(
                        dataset = dataset,
                        progress = progress,
                        selectedWorldId = selectedWorldId,
                        mapFilter = mapFilter,
                        onSelectWorld = viewModel::selectWorld,
                        onSelectFilter = viewModel::selectMapFilter,
                        onOpenStation = { id ->
                            viewModel.openStation(id)
                            navController.navigate(StationRoute(id))
                        },
                        onOpenProfile = { showProfile = true },
                        onOpenDashboard = { navController.navigate(DashboardRoute) },
                        onOpenCertificate = { showCertificate = true },
                        onOpenLibrary = { showLibrary = true }
                    )
                }

                composable<DashboardRoute> {
                    ProgressDashboardScreen(
                        dataset = dataset,
                        progress = progress,
                        onBack = { navController.popBackStack() },
                        onOpenStation = { id ->
                            viewModel.openStation(id)
                            navController.navigate(StationRoute(id))
                        },
                        onUpdateDailyReminder = viewModel::updateDailyReminder
                    )
                }

                composable<StationRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<StationRoute>()
                    val activeHadith = viewModel.getHadith(stationState.activeHadithId)
                        ?: viewModel.getHadith(route.hadithId)
                        ?: dataset.hadiths.first()

                    StationScreen(
                        hadith = activeHadith,
                        allHadiths = dataset.hadiths,
                        progress = progress,
                        stationState = stationState,
                        audioState = audioState,
                        recitationState = recitationState,
                        recorderState = recorderState,
                        onBackToMap = {
                            viewModel.onExitStation()
                            navController.popBackStack()
                        },
                        onNavigateHadith = { nextId ->
                            viewModel.openStation(nextId)
                        },
                        onSelectTab = viewModel::switchTab,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onPlayQariFromStart = viewModel::playQariFromStart,
                        onSeekAudio = viewModel::seekAudio,
                        onRewind5s = viewModel::rewind5Seconds,
                        onForward5s = viewModel::forward5Seconds,
                        onCycleSpeed = viewModel::cycleSpeed,
                        onCycleLoop = viewModel::cycleLoopMode,
                        onSelectFontSize = viewModel::setFontSize,
                        onSelectMemorizeMode = viewModel::setMemorizeMode,
                        onStartLiveRecitation = viewModel::startLiveRecitation,
                        onStopLiveRecitation = viewModel::stopLiveRecitation,
                        onRetryAfterMistake = viewModel::retryLiveRecitationAfterMistake,
                        onHintCurrentWord = viewModel::hintCurrentRecitationWord,
                        onSkipCurrentWord = viewModel::skipAndRevealCurrentRecitationWord,
                        onResetLiveRecitation = viewModel::resetLiveRecitation,
                        onSetPauseOnMistake = viewModel::setRecitationPauseOnMistake,
                        onPlayWordOrPhraseHint = viewModel::playWordOrPhraseHint,
                        onSetMemHidePercent = viewModel::setMemHidePercent,
                        onReshuffleHidden = viewModel::reshuffleHiddenWords,
                        onToggleFirstLetterHint = viewModel::toggleFirstLetterHint,
                        onTogglePeekWord = viewModel::togglePeekWord,
                        onRevealNextWord = viewModel::revealNextHiddenWord,
                        onRevealAllWords = viewModel::revealAllHiddenWords,
                        onStartSelfRecording = viewModel::startSelfRecording,
                        onStopSelfRecording = viewModel::stopSelfRecording,
                        onToggleSelfPlayback = viewModel::toggleSelfRecordingPlayback,
                        onSelectQuizMode = viewModel::setQuizMode,
                        onSubmitQuizAnswer = viewModel::submitQuizAnswer,
                        onWordPuzzleSolved = viewModel::onWordPuzzleSolved,
                        onVocabMatchSolved = viewModel::onVocabMatchSolved,
                        onGameCorrectSfx = viewModel::playGameCorrectSfx,
                        onGameWrongSfx = viewModel::playGameWrongSfx,
                        onOpenCertificate = { showCertificate = true }
                    )
                }
            }

            if (showProfile) {
                StudentProfileDialog(
                    progress = progress,
                    totalHadiths = dataset.hadiths.size.coerceAtLeast(42),
                    onSaveProfile = viewModel::updateStudentProfile,
                    onToggleTeacherMode = viewModel::toggleTeacherMode,
                    onToggleSfx = viewModel::toggleSfxMute,
                    onResetProgress = viewModel::resetAllProgress,
                    onDismiss = { showProfile = false }
                )
            }

            if (showCertificate) {
                CertificateDialog(
                    progress = progress,
                    totalHadiths = dataset.hadiths.size.coerceAtLeast(42),
                    onNameChange = viewModel::updateStudentName,
                    onDismiss = { showCertificate = false }
                )
            }

                if (showLibrary) {
                    SunnahLibraryDialog(
                        books = viewModel.books,
                        completedNawawiCount = progress.completedIds.size,
                        onDismiss = { showLibrary = false }
                    )
                }
            }
        }
    }
}

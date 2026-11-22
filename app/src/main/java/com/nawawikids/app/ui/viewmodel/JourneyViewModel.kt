package com.nawawikids.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nawawikids.app.audio.AudioPlaybackState
import com.nawawikids.app.audio.HadithAudioController
import com.nawawikids.app.audio.SfxKind
import com.nawawikids.app.audio.SoundEffectsPlayer
import com.nawawikids.app.audio.VoiceRecorderController
import com.nawawikids.app.audio.VoiceRecorderState
import com.nawawikids.app.data.SunnahRepository
import com.nawawikids.app.model.AvatarChoice
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.HadithDataset
import com.nawawikids.app.model.MapFilter
import com.nawawikids.app.model.MemorizeMode
import com.nawawikids.app.model.QuizMode
import com.nawawikids.app.model.StationTab
import com.nawawikids.app.model.SunnahBook
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.speech.LiveRecitationController
import com.nawawikids.app.speech.LiveRecitationState
import com.nawawikids.app.util.ArabicUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DatasetLoadState {
    data object Loading : DatasetLoadState
    data class Ready(val dataset: HadithDataset) : DatasetLoadState
    data class Error(val message: String) : DatasetLoadState
}

data class StationUiState(
    val activeHadithId: Int = 1,
    val activeTab: StationTab = StationTab.LISTEN,
    val visitedTabs: Set<StationTab> = setOf(StationTab.LISTEN),
    val memorizeMode: MemorizeMode = MemorizeMode.LIVE_VOICE,
    val quizMode: QuizMode = QuizMode.HERO_QUIZ,
    val memHidePercent: Int = 35,
    val memSeed: Int = 1,
    val memHiddenIndices: Set<Int> = emptySet(),
    val memPeekedIndices: Set<Int> = emptySet(),
    val firstLetterHint: Boolean = false,
    val quizSolvedQuestions: Set<Int> = emptySet(),
    val quizWrongOptionsByQuestion: Map<Int, Set<Int>> = emptyMap(),
    val lastEarnedHadithId: Int? = null
)

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SunnahRepository(application)
    private val sfxPlayer = SoundEffectsPlayer(viewModelScope)
    private val audioController = HadithAudioController(application, viewModelScope)
    private val recorderController = VoiceRecorderController(application, viewModelScope)
    private lateinit var liveRecitationController: LiveRecitationController

    val books: List<SunnahBook> = repository.sunnahBooks

    private val _datasetState = MutableStateFlow<DatasetLoadState>(DatasetLoadState.Loading)
    val datasetState: StateFlow<DatasetLoadState> = _datasetState.asStateFlow()

    private val _progress = MutableStateFlow(UserProgress())
    val progress: StateFlow<UserProgress> = _progress.asStateFlow()

    private val _progressLoaded = MutableStateFlow(false)
    val progressLoaded: StateFlow<Boolean> = _progressLoaded.asStateFlow()

    private val _station = MutableStateFlow(StationUiState())
    val station: StateFlow<StationUiState> = _station.asStateFlow()

    private val _selectedWorldId = MutableStateFlow(1)
    val selectedWorldId: StateFlow<Int> = _selectedWorldId.asStateFlow()

    private val _mapFilter = MutableStateFlow(MapFilter.ALL)
    val mapFilter: StateFlow<MapFilter> = _mapFilter.asStateFlow()

    val audioState: StateFlow<AudioPlaybackState> = audioController.state
    val recitationState: StateFlow<LiveRecitationState>
        get() = liveRecitationController.state
    val recorderState: StateFlow<VoiceRecorderState> = recorderController.state

    private var hadithsById: Map<Int, Hadith> = emptyMap()
    private var initialWorldSynced = false

    init {
        liveRecitationController = LiveRecitationController(
            context = application,
            onWordRevealed = { sfxPlayer.play(SfxKind.CORRECT) },
            onMistakeDetected = { sfxPlayer.play(SfxKind.WRONG) },
            onRecitationCompleted = { hadithId ->
                val accuracy = liveRecitationController.state.value.accuracyPercent
                sfxPlayer.play(SfxKind.UNLOCK)
                viewModelScope.launch { repository.markRecitationMastered(hadithId, accuracy) }
            }
        )

        viewModelScope.launch {
            repository.touchDailyActivity()
        }

        viewModelScope.launch {
            repository.progressFlow.collect { prog ->
                _progress.value = prog
                _progressLoaded.value = true
                sfxPlayer.muted = prog.sfxMuted
                if (!initialWorldSynced && hadithsById.isNotEmpty()) {
                    syncSelectedWorldToFrontier(prog)
                }
            }
        }
        loadDataset()
    }

    private fun syncSelectedWorldToFrontier(prog: UserProgress) {
        val frontierHadith = hadithsById[prog.frontierId(hadithsById.size.coerceAtLeast(42))]
        if (frontierHadith != null) {
            _selectedWorldId.value = frontierHadith.stage
            initialWorldSynced = true
        }
    }

    fun loadDataset() {
        viewModelScope.launch {
            _datasetState.value = DatasetLoadState.Loading
            try {
                val data = repository.loadBookDataset("nawawi_40")
                hadithsById = data.hadiths.associateBy { it.id }
                _datasetState.value = DatasetLoadState.Ready(data)
                if (!initialWorldSynced) {
                    syncSelectedWorldToFrontier(_progress.value)
                }
            } catch (e: Exception) {
                _datasetState.value = DatasetLoadState.Error(
                    e.message ?: "تعذر تحميل بيانات الأربعين النووية"
                )
            }
        }
    }

    fun selectWorld(worldId: Int) {
        sfxPlayer.play(SfxKind.POP)
        _selectedWorldId.value = worldId.coerceIn(1, 4)
    }

    fun selectMapFilter(filter: MapFilter) {
        sfxPlayer.play(SfxKind.POP)
        _mapFilter.value = filter
    }

    fun getHadith(id: Int): Hadith? = hadithsById[id]

    fun openStation(hadithId: Int) {
        val h = hadithsById[hadithId] ?: return
        stopAllMedia()
        sfxPlayer.play(SfxKind.POP)

        audioController.loadHadith(h)
        liveRecitationController.prepareForHadith(h.id, h.words)
        recorderController.resetForNewHadith()

        val savedRecord = _progress.value.recordFor(h.id)
        val restoredVisited = buildSet {
            add(StationTab.LISTEN)
            if (savedRecord.learned) add(StationTab.LEARN)
            if (savedRecord.memorized) add(StationTab.MEMORIZE)
            if (savedRecord.quizCompleted) add(StationTab.QUIZ)
        }

        val initialHidePct = 35
        val hidden = ArabicUtils.computeHiddenIndices(h.words.size, h.id, 1, initialHidePct)

        _selectedWorldId.value = h.stage
        _station.value = StationUiState(
            activeHadithId = h.id,
            activeTab = StationTab.LISTEN,
            visitedTabs = restoredVisited,
            memorizeMode = MemorizeMode.LIVE_VOICE,
            quizMode = QuizMode.HERO_QUIZ,
            memHidePercent = initialHidePct,
            memSeed = 1,
            memHiddenIndices = hidden,
            memPeekedIndices = emptySet(),
            firstLetterHint = false,
            quizSolvedQuestions = emptySet(),
            quizWrongOptionsByQuestion = emptyMap(),
            lastEarnedHadithId = null
        )

        viewModelScope.launch {
            repository.setCurrentHadith(h.id)
            repository.markStepVisited(h.id, listened = true)
        }
    }

    fun onExitStation() {
        stopAllMedia()
    }

    fun switchTab(tab: StationTab) {
        if (tab != StationTab.MEMORIZE) {
            liveRecitationController.stopListening()
            recorderController.stopRecording()
            recorderController.stopPlayback()
        }
        sfxPlayer.play(SfxKind.POP)
        _station.update {
            it.copy(
                activeTab = tab,
                visitedTabs = it.visitedTabs + tab
            )
        }
        val activeId = _station.value.activeHadithId
        if (tab == StationTab.LEARN) {
            viewModelScope.launch { repository.markStepVisited(activeId, learned = true) }
        }
    }

    fun setMemorizeMode(mode: MemorizeMode) {
        liveRecitationController.stopListening()
        recorderController.stopRecording()
        recorderController.stopPlayback()
        sfxPlayer.play(SfxKind.POP)
        _station.update { it.copy(memorizeMode = mode) }
    }

    fun setQuizMode(mode: QuizMode) {
        sfxPlayer.play(SfxKind.POP)
        _station.update { it.copy(quizMode = mode) }
    }

    // ---- Student Profile & Settings ----

    fun updateStudentProfile(name: String, age: Int?, avatar: AvatarChoice) {
        sfxPlayer.play(SfxKind.CORRECT)
        viewModelScope.launch { repository.saveStudentProfile(name, age, avatar) }
    }

    fun completeOnboarding(name: String, age: Int?, avatar: AvatarChoice) {
        sfxPlayer.play(SfxKind.UNLOCK)
        viewModelScope.launch { repository.completeOnboarding(name, age, avatar) }
    }

    fun skipOnboarding() {
        sfxPlayer.play(SfxKind.POP)
        viewModelScope.launch { repository.markOnboardingSeen() }
    }

    fun updateStudentName(name: String) {
        viewModelScope.launch { repository.saveStudentName(name) }
    }

    fun toggleTeacherMode(enabled: Boolean) {
        sfxPlayer.play(SfxKind.POP)
        viewModelScope.launch { repository.setTeacherMode(enabled) }
    }

    fun toggleSfxMute() {
        val next = !_progress.value.sfxMuted
        sfxPlayer.muted = next
        if (!next) sfxPlayer.play(SfxKind.POP)
        viewModelScope.launch { repository.setSfxMuted(next) }
    }

    fun setFontSize(option: FontSizeOption) {
        sfxPlayer.play(SfxKind.POP)
        viewModelScope.launch { repository.setFontSize(option) }
    }

    fun resetAllProgress() {
        stopAllMedia()
        sfxPlayer.play(SfxKind.POP)
        _selectedWorldId.value = 1
        viewModelScope.launch { repository.resetProgress() }
    }

    // ---- Audio Controls ----

    fun togglePlayPause() {
        liveRecitationController.stopListening()
        recorderController.stopPlayback()
        audioController.togglePlayPause()
        viewModelScope.launch {
            repository.markStepVisited(_station.value.activeHadithId, listened = true)
        }
    }

    fun playQariFromStart() {
        liveRecitationController.stopListening()
        recorderController.stopPlayback()
        audioController.playFromStart()
    }

    fun seekAudio(seconds: Double, playImmediately: Boolean = false) {
        if (playImmediately) {
            liveRecitationController.stopListening()
            recorderController.stopPlayback()
        }
        audioController.seekToSeconds(seconds, playImmediately)
    }

    fun rewind5Seconds() = audioController.rewind5Seconds()

    fun forward5Seconds() = audioController.forward5Seconds()

    fun playParagraph(paragraphKey: Int, repeat: Boolean = false) {
        liveRecitationController.stopListening()
        recorderController.stopPlayback()
        audioController.playParagraph(paragraphKey, repeat)
    }

    fun toggleRepeatCurrentParagraph() {
        sfxPlayer.play(SfxKind.POP)
        audioController.toggleRepeatCurrentParagraph()
    }

    fun skipToPrevParagraph() {
        sfxPlayer.play(SfxKind.POP)
        audioController.skipToPreviousParagraph()
    }

    fun skipToNextParagraph() {
        sfxPlayer.play(SfxKind.POP)
        audioController.skipToNextParagraph()
    }

    fun cycleSpeed() {
        sfxPlayer.play(SfxKind.POP)
        audioController.cycleSpeed()
    }

    fun cycleLoopMode() {
        sfxPlayer.play(SfxKind.POP)
        audioController.cycleLoopMode()
    }

    fun playWordOrPhraseHint(wordIndex: Int, wordCount: Int = 1) {
        val h = hadithsById[_station.value.activeHadithId] ?: return
        if (wordIndex !in h.words.indices) return
        liveRecitationController.stopListening()
        recorderController.stopPlayback()
        val endIdx = (wordIndex + wordCount - 1).coerceAtMost(h.words.lastIndex)
        audioController.playSlice(h.words[wordIndex].s, h.words[endIdx].e)
    }

    // ---- Live Voice Recitation ----

    fun startLiveRecitation() {
        audioController.pause()
        recorderController.stopRecording()
        recorderController.stopPlayback()
        liveRecitationController.startListening()
    }

    fun stopLiveRecitation() = liveRecitationController.stopListening()

    fun retryLiveRecitationAfterMistake() {
        audioController.pause()
        liveRecitationController.retryAfterMistake()
    }

    fun hintCurrentRecitationWord() {
        sfxPlayer.play(SfxKind.POP)
        liveRecitationController.hintCurrentWord()
    }

    fun skipAndRevealCurrentRecitationWord() {
        audioController.pause()
        liveRecitationController.revealCurrentWordAndContinue()
    }

    fun resetLiveRecitation() {
        audioController.pause()
        sfxPlayer.play(SfxKind.POP)
        liveRecitationController.resetRecitation()
    }

    fun jumpRecitationToWord(wordIndex: Int) {
        audioController.pause()
        sfxPlayer.play(SfxKind.POP)
        liveRecitationController.jumpToWordIndex(wordIndex)
    }

    fun setRecitationPauseOnMistake(enabled: Boolean) {
        sfxPlayer.play(SfxKind.POP)
        liveRecitationController.setPauseOnMistake(enabled)
    }

    // ---- Progressive Masking & Voice Recorder ----

    fun setMemHidePercent(pct: Int) {
        val h = hadithsById[_station.value.activeHadithId] ?: return
        sfxPlayer.play(SfxKind.POP)
        val hidden = ArabicUtils.computeHiddenIndices(h.words.size, h.id, _station.value.memSeed, pct)
        _station.update {
            it.copy(
                memHidePercent = pct,
                memHiddenIndices = hidden,
                memPeekedIndices = emptySet()
            )
        }
    }

    fun reshuffleHiddenWords() {
        val h = hadithsById[_station.value.activeHadithId] ?: return
        sfxPlayer.play(SfxKind.POP)
        val nextSeed = _station.value.memSeed + 1
        val pct = if (_station.value.memHidePercent == 0) 35 else _station.value.memHidePercent
        val hidden = ArabicUtils.computeHiddenIndices(h.words.size, h.id, nextSeed, pct)
        _station.update {
            it.copy(
                memHidePercent = pct,
                memSeed = nextSeed,
                memHiddenIndices = hidden,
                memPeekedIndices = emptySet()
            )
        }
    }

    fun toggleFirstLetterHint(enabled: Boolean) {
        _station.update { it.copy(firstLetterHint = enabled) }
    }

    fun togglePeekWord(wordIndex: Int) {
        _station.update { curr ->
            if (!curr.memHiddenIndices.contains(wordIndex)) return@update curr
            val nextPeeked = if (curr.memPeekedIndices.contains(wordIndex)) {
                curr.memPeekedIndices - wordIndex
            } else {
                sfxPlayer.play(SfxKind.POP)
                curr.memPeekedIndices + wordIndex
            }
            curr.copy(memPeekedIndices = nextPeeked)
        }
    }

    fun revealNextHiddenWord() {
        val curr = _station.value
        val nextUnpeeked = curr.memHiddenIndices.sorted().firstOrNull { !curr.memPeekedIndices.contains(it) }
            ?: return
        sfxPlayer.play(SfxKind.POP)
        _station.update { it.copy(memPeekedIndices = it.memPeekedIndices + nextUnpeeked) }
    }

    fun revealAllHiddenWords() {
        sfxPlayer.play(SfxKind.POP)
        _station.update { it.copy(memPeekedIndices = it.memHiddenIndices) }
    }

    fun startSelfRecording() {
        audioController.pause()
        liveRecitationController.stopListening()
        recorderController.startRecording(_station.value.activeHadithId)
    }

    fun stopSelfRecording() {
        recorderController.stopRecording()
        revealAllHiddenWords()
    }

    fun toggleSelfRecordingPlayback() {
        audioController.pause()
        recorderController.togglePlayback()
    }

    // ---- Step 4: Hero Quiz & Mini-Games ----

    fun submitQuizAnswer(questionIndex: Int, optionIndex: Int) {
        val h = hadithsById[_station.value.activeHadithId] ?: return
        val ex = h.exercises.getOrNull(questionIndex) ?: return
        if (_station.value.quizSolvedQuestions.contains(questionIndex)) return

        if (optionIndex == ex.correct) {
            val nextSolved = _station.value.quizSolvedQuestions + questionIndex
            _station.update { it.copy(quizSolvedQuestions = nextSolved) }
            if (nextSolved.size >= h.exercises.size) {
                sfxPlayer.play(SfxKind.UNLOCK)
                _station.update { it.copy(lastEarnedHadithId = h.id) }
                viewModelScope.launch {
                    repository.markHadithCompleted(h.id, totalHadiths = hadithsById.size.coerceAtLeast(42))
                }
            } else {
                sfxPlayer.play(SfxKind.CORRECT)
            }
        } else {
            sfxPlayer.play(SfxKind.WRONG)
            _station.update { curr ->
                val curWrong = curr.quizWrongOptionsByQuestion[questionIndex] ?: emptySet()
                curr.copy(
                    quizWrongOptionsByQuestion = curr.quizWrongOptionsByQuestion + (questionIndex to (curWrong + optionIndex))
                )
            }
        }
    }

    fun onWordPuzzleSolved() {
        sfxPlayer.play(SfxKind.UNLOCK)
        viewModelScope.launch { repository.markPuzzleSolved(_station.value.activeHadithId) }
    }

    fun onVocabMatchSolved() {
        sfxPlayer.play(SfxKind.UNLOCK)
        viewModelScope.launch { repository.markVocabSolved(_station.value.activeHadithId) }
    }

    fun playGameCorrectSfx() = sfxPlayer.play(SfxKind.CORRECT)
    fun playGameWrongSfx() = sfxPlayer.play(SfxKind.WRONG)

    private fun stopAllMedia() {
        audioController.stopAndReset()
        liveRecitationController.stopListening()
        recorderController.stopRecording()
        recorderController.stopPlayback()
    }

    override fun onCleared() {
        super.onCleared()
        audioController.release()
        liveRecitationController.release()
        recorderController.release()
    }
}

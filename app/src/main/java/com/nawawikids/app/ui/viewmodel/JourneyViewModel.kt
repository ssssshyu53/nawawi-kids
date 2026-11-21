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
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.HadithDataset
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
    // Progressive memorization state
    val memHidePercent: Int = 35,
    val memSeed: Int = 1,
    val memHiddenIndices: Set<Int> = emptySet(),
    val memPeekedIndices: Set<Int> = emptySet(),
    val firstLetterHint: Boolean = false,
    // Quiz state for current hadith
    val quizSolvedQuestions: Set<Int> = emptySet(),
    val quizWrongOptionsByQuestion: Map<Int, Set<Int>> = emptyMap(),
    val lastEarnedHadithId: Int? = null
)

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SunnahRepository(application)
    private val sfxPlayer = SoundEffectsPlayer(viewModelScope)
    private val audioController = HadithAudioController(application, viewModelScope)
    private val recorderController = VoiceRecorderController(application, viewModelScope)
    private val liveRecitationController = LiveRecitationController(
        context = application,
        onWordRevealed = { sfxPlayer.play(SfxKind.CORRECT) },
        onMistakeDetected = { sfxPlayer.play(SfxKind.WRONG) },
        onRecitationCompleted = { hadithId ->
            sfxPlayer.play(SfxKind.UNLOCK)
            viewModelScope.launch { repository.markRecitationMastered(hadithId) }
        }
    )

    val books: List<SunnahBook> = repository.sunnahBooks

    private val _datasetState = MutableStateFlow<DatasetLoadState>(DatasetLoadState.Loading)
    val datasetState: StateFlow<DatasetLoadState> = _datasetState.asStateFlow()

    private val _progress = MutableStateFlow(UserProgress())
    val progress: StateFlow<UserProgress> = _progress.asStateFlow()

    private val _station = MutableStateFlow(StationUiState())
    val station: StateFlow<StationUiState> = _station.asStateFlow()

    val audioState: StateFlow<AudioPlaybackState> = audioController.state
    val recitationState: StateFlow<LiveRecitationState> = liveRecitationController.state
    val recorderState: StateFlow<VoiceRecorderState> = recorderController.state

    private var hadithsById: Map<Int, Hadith> = emptyMap()

    init {
        viewModelScope.launch {
            repository.progressFlow.collect { prog ->
                _progress.value = prog
                sfxPlayer.muted = prog.sfxMuted
            }
        }
        loadDataset()
    }

    fun loadDataset() {
        viewModelScope.launch {
            _datasetState.value = DatasetLoadState.Loading
            try {
                val data = repository.loadBookDataset("nawawi_40")
                hadithsById = data.hadiths.associateBy { it.id }
                _datasetState.value = DatasetLoadState.Ready(data)
            } catch (e: Exception) {
                _datasetState.value = DatasetLoadState.Error(
                    e.message ?: "تعذر تحميل بيانات الأربعين النووية"
                )
            }
        }
    }

    fun getHadith(id: Int): Hadith? = hadithsById[id]

    fun getAllHadiths(): List<Hadith> =
        (_datasetState.value as? DatasetLoadState.Ready)?.dataset?.hadiths ?: emptyList()

    fun openStation(hadithId: Int) {
        val h = hadithsById[hadithId] ?: return
        stopAllMedia()
        sfxPlayer.play(SfxKind.POP)

        audioController.loadHadith(h)
        liveRecitationController.prepareForHadith(h.id, h.words)
        recorderController.resetForNewHadith()

        val initialHidePct = 35
        val hidden = ArabicUtils.computeHiddenIndices(h.words.size, h.id, 1, initialHidePct)

        _station.value = StationUiState(
            activeHadithId = h.id,
            activeTab = StationTab.LISTEN,
            visitedTabs = setOf(StationTab.LISTEN),
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

        viewModelScope.launch { repository.setCurrentHadith(h.id) }
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
        if (tab != StationTab.LISTEN) {
            audioController.pause()
        }
        sfxPlayer.play(SfxKind.POP)
        _station.update {
            it.copy(
                activeTab = tab,
                visitedTabs = it.visitedTabs + tab
            )
        }
    }

    fun setMemorizeMode(mode: MemorizeMode) {
        liveRecitationController.stopListening()
        recorderController.stopRecording()
        recorderController.stopPlayback()
        audioController.pause()
        sfxPlayer.play(SfxKind.POP)
        _station.update { it.copy(memorizeMode = mode) }
    }

    fun setQuizMode(mode: QuizMode) {
        sfxPlayer.play(SfxKind.POP)
        _station.update { it.copy(quizMode = mode) }
    }

    // ---- Progress & Settings ----

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
        viewModelScope.launch { repository.resetProgress() }
    }

    // ---- Audio Controls (Step 1 & Recitation Helper) ----

    fun togglePlayPause() {
        liveRecitationController.stopListening()
        recorderController.stopPlayback()
        audioController.togglePlayPause()
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
    fun cycleSpeed() {
        sfxPlayer.play(SfxKind.POP)
        audioController.cycleSpeed()
    }

    fun cycleLoopMode() {
        sfxPlayer.play(SfxKind.POP)
        audioController.cycleLoopMode()
    }

    /**
     * Plays the Qari's exact pronunciation of the current target word (or a short 4-word phrase)
     * during Live Voice Recitation so the child can listen and repeat accurately!
     */
    fun playWordOrPhraseHint(wordIndex: Int, wordCount: Int = 1) {
        val h = hadithsById[_station.value.activeHadithId] ?: return
        if (wordIndex !in h.words.indices) return
        liveRecitationController.stopListening()
        recorderController.stopPlayback()
        val endIdx = (wordIndex + wordCount - 1).coerceAtMost(h.words.lastIndex)
        audioController.playSlice(h.words[wordIndex].s, h.words[endIdx].e)
    }

    // ---- Live Voice Recitation Controls (Step 3 Mode A) ----

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

    // ---- Progressive Memorization & Voice Recorder (Step 3 Mode B) ----

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
    fun playPopSfx() = sfxPlayer.play(SfxKind.POP)

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

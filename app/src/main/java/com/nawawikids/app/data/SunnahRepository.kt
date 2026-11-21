package com.nawawikids.app.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.HadithDataset
import com.nawawikids.app.model.SunnahBook
import com.nawawikids.app.model.UserProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val Context.nawawiDataStore: DataStore<Preferences> by preferencesDataStore(name = "nawawi_kids_progress")

/**
 * Repository for Sunnah Books datasets (extensible beyond Al-Arba'oon Al-Nawawiyyah)
 * and local DataStore persistence of the child's journey progress.
 */
class SunnahRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var cachedNawawiDataset: HadithDataset? = null

    val sunnahBooks: List<SunnahBook> = listOf(
        SunnahBook(
            id = "nawawi_40",
            title = "الأربعون النووية",
            author = "الإمام يحيى بن شرف النووي",
            subtitle = "٤٢ حديثاً جامعاً لأصول الدين والأخلاق في رحلة قصصية تفاعلية كاملة",
            hadithCount = 42,
            assetDataPath = "data/hadiths.json",
            isAvailable = true,
            badgeLabel = "متاح بالكامل",
            accentColor = Color(0xFF14B8A6),
            accentDark = Color(0xFF0F766E),
            bgTint = Color(0xFFF0FDFA)
        ),
        SunnahBook(
            id = "riyad_salihin",
            title = "مختارات رياض الصالحين",
            author = "الإمام النووي",
            subtitle = "أحاديث الآداب والرقائق والأذكار اليومية المبسطة للناشئة",
            hadithCount = 60,
            assetDataPath = null,
            isAvailable = false,
            badgeLabel = "قريباً في التوسعة",
            accentColor = Color(0xFFF59E0B),
            accentDark = Color(0xFFB45309),
            bgTint = Color(0xFFFFFBEB)
        ),
        SunnahBook(
            id = "adab_mufrad",
            title = "الأدب المفرد للأطفال",
            author = "الإمام البخاري",
            subtitle = "بر الوالدين وصلة الرحم وآداب التعامل والصحبة الصالحة",
            hadithCount = 50,
            assetDataPath = null,
            isAvailable = false,
            badgeLabel = "قريباً في التوسعة",
            accentColor = Color(0xFFF43F5E),
            accentDark = Color(0xFFBE123C),
            bgTint = Color(0xFFFFF1F2)
        ),
        SunnahBook(
            id = "umdat_ahkam",
            title = "عمدة الأحكام الميسر",
            author = "الحافظ عبد الغني المقدسي",
            subtitle = "أحاديث الطهارة والصلاة والعبادات بأسلوب قصصي مشوّق",
            hadithCount = 40,
            assetDataPath = null,
            isAvailable = false,
            badgeLabel = "قريباً في التوسعة",
            accentColor = Color(0xFF0EA5E9),
            accentDark = Color(0xFF0369A1),
            bgTint = Color(0xFFF0F9FF)
        )
    )

    suspend fun loadBookDataset(bookId: String = "nawawi_40"): HadithDataset = withContext(Dispatchers.IO) {
        if (bookId == "nawawi_40" && cachedNawawiDataset != null) {
            return@withContext cachedNawawiDataset!!
        }
        val book = sunnahBooks.firstOrNull { it.id == bookId && it.assetDataPath != null }
            ?: sunnahBooks.first()
        val raw = context.assets.open(book.assetDataPath!!).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val parsed = json.decodeFromString<HadithDataset>(raw)
        if (book.id == "nawawi_40") {
            cachedNawawiDataset = parsed
        }
        parsed
    }

    private object Keys {
        val COMPLETED_IDS = stringSetPreferencesKey("completed_ids")
        val RECITED_IDS = stringSetPreferencesKey("recited_ids")
        val PUZZLE_IDS = stringSetPreferencesKey("puzzle_solved_ids")
        val VOCAB_IDS = stringSetPreferencesKey("vocab_solved_ids")
        val UNLOCKED_UP_TO = intPreferencesKey("unlocked_up_to")
        val CURRENT_ID = intPreferencesKey("current_id")
        val TEACHER_MODE = booleanPreferencesKey("teacher_mode")
        val SFX_MUTED = booleanPreferencesKey("sfx_muted")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val STUDENT_NAME = stringPreferencesKey("student_name")
    }

    val progressFlow: Flow<UserProgress> = context.nawawiDataStore.data.map { prefs ->
        UserProgress(
            completedIds = prefs[Keys.COMPLETED_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            recitedIds = prefs[Keys.RECITED_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            puzzleSolvedIds = prefs[Keys.PUZZLE_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            vocabSolvedIds = prefs[Keys.VOCAB_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            unlockedUpTo = (prefs[Keys.UNLOCKED_UP_TO] ?: 1).coerceIn(1, 42),
            currentId = (prefs[Keys.CURRENT_ID] ?: 1).coerceIn(1, 42),
            teacherMode = prefs[Keys.TEACHER_MODE] ?: false,
            sfxMuted = prefs[Keys.SFX_MUTED] ?: false,
            fontSize = FontSizeOption.fromKey(prefs[Keys.FONT_SIZE]),
            studentName = prefs[Keys.STUDENT_NAME] ?: ""
        )
    }

    suspend fun saveStudentName(name: String) {
        context.nawawiDataStore.edit { it[Keys.STUDENT_NAME] = name.take(40) }
    }

    suspend fun setCurrentHadith(id: Int) {
        context.nawawiDataStore.edit { it[Keys.CURRENT_ID] = id.coerceIn(1, 42) }
    }

    suspend fun setTeacherMode(enabled: Boolean) {
        context.nawawiDataStore.edit { it[Keys.TEACHER_MODE] = enabled }
    }

    suspend fun setSfxMuted(muted: Boolean) {
        context.nawawiDataStore.edit { it[Keys.SFX_MUTED] = muted }
    }

    suspend fun setFontSize(option: FontSizeOption) {
        context.nawawiDataStore.edit { it[Keys.FONT_SIZE] = option.key }
    }

    suspend fun markHadithCompleted(hadithId: Int, totalHadiths: Int = 42) {
        context.nawawiDataStore.edit { prefs ->
            val currentCompleted = prefs[Keys.COMPLETED_IDS] ?: emptySet()
            prefs[Keys.COMPLETED_IDS] = currentCompleted + hadithId.toString()
            val curUnlocked = prefs[Keys.UNLOCKED_UP_TO] ?: 1
            val nextUnlock = (hadithId + 1).coerceAtMost(totalHadiths)
            if (nextUnlock > curUnlocked) {
                prefs[Keys.UNLOCKED_UP_TO] = nextUnlock
            }
        }
    }

    suspend fun markRecitationMastered(hadithId: Int) {
        context.nawawiDataStore.edit { prefs ->
            val cur = prefs[Keys.RECITED_IDS] ?: emptySet()
            prefs[Keys.RECITED_IDS] = cur + hadithId.toString()
        }
    }

    suspend fun markPuzzleSolved(hadithId: Int) {
        context.nawawiDataStore.edit { prefs ->
            val cur = prefs[Keys.PUZZLE_IDS] ?: emptySet()
            prefs[Keys.PUZZLE_IDS] = cur + hadithId.toString()
        }
    }

    suspend fun markVocabSolved(hadithId: Int) {
        context.nawawiDataStore.edit { prefs ->
            val cur = prefs[Keys.VOCAB_IDS] ?: emptySet()
            prefs[Keys.VOCAB_IDS] = cur + hadithId.toString()
        }
    }

    suspend fun resetProgress() {
        context.nawawiDataStore.edit { prefs ->
            val keepName = prefs[Keys.STUDENT_NAME] ?: ""
            val keepSfx = prefs[Keys.SFX_MUTED] ?: false
            val keepFont = prefs[Keys.FONT_SIZE] ?: FontSizeOption.MEDIUM.key
            prefs.clear()
            prefs[Keys.STUDENT_NAME] = keepName
            prefs[Keys.SFX_MUTED] = keepSfx
            prefs[Keys.FONT_SIZE] = keepFont
            prefs[Keys.UNLOCKED_UP_TO] = 1
            prefs[Keys.CURRENT_ID] = 1
        }
    }
}

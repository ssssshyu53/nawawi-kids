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
import com.nawawikids.app.model.AvatarChoice
import com.nawawikids.app.model.FontSizeOption
import com.nawawikids.app.model.HadithDataset
import com.nawawikids.app.model.HadithProgressRecord
import com.nawawikids.app.model.SunnahBook
import com.nawawikids.app.model.UserProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val Context.nawawiDataStore: DataStore<Preferences> by preferencesDataStore(name = "nawawi_kids_progress")

/**
 * Persistent repository for Sunnah Books datasets and complete student profile + per-hadith progress.
 * All student details (name, age, companion avatar, daily streak, XP, per-step completion,
 * best recitation accuracy, and unlocked stars) are saved durably in DataStore.
 */
class SunnahRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
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
        val HADITH_RECORDS_JSON = stringPreferencesKey("hadith_records_json")
        val UNLOCKED_UP_TO = intPreferencesKey("unlocked_up_to")
        val CURRENT_ID = intPreferencesKey("current_id")
        val TEACHER_MODE = booleanPreferencesKey("teacher_mode")
        val SFX_MUTED = booleanPreferencesKey("sfx_muted")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val STUDENT_AGE = intPreferencesKey("student_age")
        val AVATAR_CHOICE = stringPreferencesKey("avatar_choice")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val TOTAL_XP = intPreferencesKey("total_xp")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val progressFlow: Flow<UserProgress> = context.nawawiDataStore.data.map { prefs ->
        val rawRecords = prefs[Keys.HADITH_RECORDS_JSON]
        val parsedRecords: Map<Int, HadithProgressRecord> = if (!rawRecords.isNullOrBlank()) {
            try {
                json.decodeFromString<Map<Int, HadithProgressRecord>>(rawRecords)
            } catch (_: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        UserProgress(
            completedIds = prefs[Keys.COMPLETED_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            recitedIds = prefs[Keys.RECITED_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            puzzleSolvedIds = prefs[Keys.PUZZLE_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            vocabSolvedIds = prefs[Keys.VOCAB_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
            hadithRecords = parsedRecords,
            unlockedUpTo = (prefs[Keys.UNLOCKED_UP_TO] ?: 1).coerceIn(1, 42),
            currentId = (prefs[Keys.CURRENT_ID] ?: 1).coerceIn(1, 42),
            teacherMode = prefs[Keys.TEACHER_MODE] ?: false,
            sfxMuted = prefs[Keys.SFX_MUTED] ?: false,
            fontSize = FontSizeOption.fromKey(prefs[Keys.FONT_SIZE]),
            studentName = prefs[Keys.STUDENT_NAME] ?: "",
            studentAge = prefs[Keys.STUDENT_AGE]?.takeIf { it in 1..120 },
            avatarChoice = AvatarChoice.fromKey(prefs[Keys.AVATAR_CHOICE]),
            streakDays = (prefs[Keys.STREAK_DAYS] ?: 1).coerceAtLeast(1),
            lastActiveDate = prefs[Keys.LAST_ACTIVE_DATE] ?: "",
            totalXp = (prefs[Keys.TOTAL_XP] ?: 0).coerceAtLeast(0),
            hasSeenOnboarding = prefs[Keys.HAS_SEEN_ONBOARDING] ?: false,
            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: true,
            reminderHour = (prefs[Keys.REMINDER_HOUR] ?: 16).coerceIn(0, 23),
            reminderMinute = (prefs[Keys.REMINDER_MINUTE] ?: 0).coerceIn(0, 59)
        )
    }

    /**
     * Records today's learning session and updates the student's consecutive daily streak.
     */
    suspend fun touchDailyActivity() {
        val today = LocalDate.now()
        val todayStr = today.toString()
        context.nawawiDataStore.edit { prefs ->
            val lastStr = prefs[Keys.LAST_ACTIVE_DATE] ?: ""
            if (lastStr == todayStr) return@edit
            val currentStreak = (prefs[Keys.STREAK_DAYS] ?: 0).coerceAtLeast(0)
            val nextStreak = if (lastStr.isNotBlank()) {
                try {
                    val lastDate = LocalDate.parse(lastStr)
                    val diff = ChronoUnit.DAYS.between(lastDate, today)
                    when {
                        diff == 1L -> currentStreak + 1
                        diff <= 0L -> currentStreak.coerceAtLeast(1)
                        else -> 1
                    }
                } catch (_: Exception) {
                    1
                }
            } else {
                1
            }
            prefs[Keys.STREAK_DAYS] = nextStreak
            prefs[Keys.LAST_ACTIVE_DATE] = todayStr
        }
    }

    suspend fun saveStudentProfile(name: String, age: Int?, avatar: AvatarChoice) {
        context.nawawiDataStore.edit { prefs ->
            prefs[Keys.STUDENT_NAME] = name.trim().take(40)
            if (age != null && age in 1..120) {
                prefs[Keys.STUDENT_AGE] = age
            } else {
                prefs.remove(Keys.STUDENT_AGE)
            }
            prefs[Keys.AVATAR_CHOICE] = avatar.key
        }
    }

    suspend fun completeOnboarding(name: String, age: Int?, avatar: AvatarChoice) {
        context.nawawiDataStore.edit { prefs ->
            val cleanName = name.trim().take(40)
            if (cleanName.isNotEmpty()) {
                prefs[Keys.STUDENT_NAME] = cleanName
            }
            if (age != null && age in 1..120) {
                prefs[Keys.STUDENT_AGE] = age
            }
            prefs[Keys.AVATAR_CHOICE] = avatar.key
            prefs[Keys.HAS_SEEN_ONBOARDING] = true
        }
    }

    suspend fun markOnboardingSeen() {
        context.nawawiDataStore.edit { prefs ->
            prefs[Keys.HAS_SEEN_ONBOARDING] = true
        }
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

    suspend fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.nawawiDataStore.edit { prefs ->
            prefs[Keys.DAILY_REMINDER_ENABLED] = enabled
            prefs[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun markStepVisited(hadithId: Int, listened: Boolean = false, learned: Boolean = false) {
        updateHadithRecord(hadithId) { rec ->
            var xpGain = 0
            val nextListened = rec.listened || listened
            val nextLearned = rec.learned || learned
            if (!rec.listened && nextListened) xpGain += 10
            if (!rec.learned && nextLearned) xpGain += 10
            rec.copy(listened = nextListened, learned = nextLearned) to xpGain
        }
    }

    suspend fun markHadithCompleted(hadithId: Int, totalHadiths: Int = 42) {
        touchDailyActivity()
        context.nawawiDataStore.edit { prefs ->
            val currentCompleted = prefs[Keys.COMPLETED_IDS] ?: emptySet()
            val isNew = !currentCompleted.contains(hadithId.toString())
            prefs[Keys.COMPLETED_IDS] = currentCompleted + hadithId.toString()
            val curUnlocked = prefs[Keys.UNLOCKED_UP_TO] ?: 1
            val nextUnlock = (hadithId + 1).coerceAtMost(totalHadiths)
            if (nextUnlock > curUnlocked) {
                prefs[Keys.UNLOCKED_UP_TO] = nextUnlock
            }

            val map = decodeRecords(prefs[Keys.HADITH_RECORDS_JSON]).toMutableMap()
            val existing = map[hadithId] ?: HadithProgressRecord()
            map[hadithId] = existing.copy(quizCompleted = true)
            prefs[Keys.HADITH_RECORDS_JSON] = json.encodeToString(map)
            if (isNew) {
                prefs[Keys.TOTAL_XP] = (prefs[Keys.TOTAL_XP] ?: 0) + 50
            }
        }
    }

    suspend fun markRecitationMastered(hadithId: Int, accuracyPercent: Int) {
        touchDailyActivity()
        context.nawawiDataStore.edit { prefs ->
            val cur = prefs[Keys.RECITED_IDS] ?: emptySet()
            val isNew = !cur.contains(hadithId.toString())
            prefs[Keys.RECITED_IDS] = cur + hadithId.toString()

            val map = decodeRecords(prefs[Keys.HADITH_RECORDS_JSON]).toMutableMap()
            val existing = map[hadithId] ?: HadithProgressRecord()
            map[hadithId] = existing.copy(
                memorized = true,
                bestRecitationAccuracy = maxOf(existing.bestRecitationAccuracy, accuracyPercent.coerceIn(0, 100))
            )
            prefs[Keys.HADITH_RECORDS_JSON] = json.encodeToString(map)
            if (isNew) {
                prefs[Keys.TOTAL_XP] = (prefs[Keys.TOTAL_XP] ?: 0) + 40
            }
        }
    }

    suspend fun markPuzzleSolved(hadithId: Int) {
        touchDailyActivity()
        context.nawawiDataStore.edit { prefs ->
            val cur = prefs[Keys.PUZZLE_IDS] ?: emptySet()
            val isNew = !cur.contains(hadithId.toString())
            prefs[Keys.PUZZLE_IDS] = cur + hadithId.toString()

            val map = decodeRecords(prefs[Keys.HADITH_RECORDS_JSON]).toMutableMap()
            val existing = map[hadithId] ?: HadithProgressRecord()
            map[hadithId] = existing.copy(puzzleCompleted = true)
            prefs[Keys.HADITH_RECORDS_JSON] = json.encodeToString(map)
            if (isNew) {
                prefs[Keys.TOTAL_XP] = (prefs[Keys.TOTAL_XP] ?: 0) + 25
            }
        }
    }

    suspend fun markVocabSolved(hadithId: Int) {
        touchDailyActivity()
        context.nawawiDataStore.edit { prefs ->
            val cur = prefs[Keys.VOCAB_IDS] ?: emptySet()
            val isNew = !cur.contains(hadithId.toString())
            prefs[Keys.VOCAB_IDS] = cur + hadithId.toString()

            val map = decodeRecords(prefs[Keys.HADITH_RECORDS_JSON]).toMutableMap()
            val existing = map[hadithId] ?: HadithProgressRecord()
            map[hadithId] = existing.copy(vocabCompleted = true)
            prefs[Keys.HADITH_RECORDS_JSON] = json.encodeToString(map)
            if (isNew) {
                prefs[Keys.TOTAL_XP] = (prefs[Keys.TOTAL_XP] ?: 0) + 25
            }
        }
    }

    private suspend fun updateHadithRecord(
        hadithId: Int,
        transform: (HadithProgressRecord) -> Pair<HadithProgressRecord, Int>
    ) {
        context.nawawiDataStore.edit { prefs ->
            val map = decodeRecords(prefs[Keys.HADITH_RECORDS_JSON]).toMutableMap()
            val existing = map[hadithId] ?: HadithProgressRecord()
            val (updated, xpGain) = transform(existing)
            if (updated != existing) {
                map[hadithId] = updated
                prefs[Keys.HADITH_RECORDS_JSON] = json.encodeToString(map)
            }
            if (xpGain > 0) {
                prefs[Keys.TOTAL_XP] = (prefs[Keys.TOTAL_XP] ?: 0) + xpGain
            }
        }
    }

    private fun decodeRecords(raw: String?): Map<Int, HadithProgressRecord> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun resetProgress() {
        context.nawawiDataStore.edit { prefs ->
            val keepName = prefs[Keys.STUDENT_NAME] ?: ""
            val keepAge = prefs[Keys.STUDENT_AGE]
            val keepAvatar = prefs[Keys.AVATAR_CHOICE] ?: AvatarChoice.BOY_OMAR.key
            val keepSfx = prefs[Keys.SFX_MUTED] ?: false
            val keepFont = prefs[Keys.FONT_SIZE] ?: FontSizeOption.MEDIUM.key
            val keepOnboarding = prefs[Keys.HAS_SEEN_ONBOARDING] ?: true
            val keepReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: true
            val keepReminderHour = prefs[Keys.REMINDER_HOUR] ?: 16
            val keepReminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0
            prefs.clear()
            prefs[Keys.STUDENT_NAME] = keepName
            if (keepAge != null && keepAge in 1..120) {
                prefs[Keys.STUDENT_AGE] = keepAge
            }
            prefs[Keys.AVATAR_CHOICE] = keepAvatar
            prefs[Keys.SFX_MUTED] = keepSfx
            prefs[Keys.FONT_SIZE] = keepFont
            prefs[Keys.HAS_SEEN_ONBOARDING] = keepOnboarding
            prefs[Keys.DAILY_REMINDER_ENABLED] = keepReminderEnabled
            prefs[Keys.REMINDER_HOUR] = keepReminderHour
            prefs[Keys.REMINDER_MINUTE] = keepReminderMinute
            prefs[Keys.UNLOCKED_UP_TO] = 1
            prefs[Keys.CURRENT_ID] = 1
            prefs[Keys.STREAK_DAYS] = 1
            prefs[Keys.TOTAL_XP] = 0
        }
    }
}

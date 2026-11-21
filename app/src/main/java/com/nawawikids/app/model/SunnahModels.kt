package com.nawawikids.app.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HadithDataset(
    val meta: DatasetMeta,
    val hadiths: List<Hadith>
)

@Serializable
data class DatasetMeta(
    val title: String,
    val subtitle: String,
    val total: Int,
    val stages: List<StageInfo>
)

@Serializable
data class StageInfo(
    val id: Int,
    val title: String,
    val range: String,
    val color: String
)

@Serializable
data class Hadith(
    val id: Int,
    @SerialName("number_ar") val numberAr: String,
    val header: String,
    val title: String,
    val stage: Int,
    val poster: String? = null,
    val audio: String,
    val duration: Double,
    @SerialName("hdr_time") val hdrTime: List<Double> = emptyList(),
    val paragraphs: List<String> = emptyList(),
    val words: List<WordTiming> = emptyList(),
    val ghareeb: List<GhareebWord> = emptyList(),
    val meaning: String = "",
    val lessons: List<String> = emptyList(),
    val source: String = "",
    val exercises: List<Exercise> = emptyList()
)

@Serializable
data class WordTiming(
    val t: String,
    val p: Int,
    val s: Double,
    val e: Double
)

@Serializable
data class GhareebWord(
    val w: String,
    val m: String
)

@Serializable
data class Exercise(
    val type: String = "mcq",
    val badge: String,
    val q: String,
    val options: List<String>,
    val correct: Int,
    val feedback: String
)

/**
 * Extensible Sunnah Book architecture so additional prophetic books
 * (Riyad as-Salihin, Al-Adab al-Mufrad, Umdat al-Ahkam, etc.) can plug directly
 * into the same Journey + Audio + Live Recitation + Games engine.
 */
data class SunnahBook(
    val id: String,
    val title: String,
    val author: String,
    val subtitle: String,
    val hadithCount: Int,
    val assetDataPath: String?,
    val isAvailable: Boolean,
    val badgeLabel: String,
    val accentColor: Color,
    val accentDark: Color,
    val bgTint: Color
)

data class ChapterVisual(
    val id: Int,
    val ord: String,
    val name: String,
    val story: String,
    val sceneAsset: String,
    val guideAsset: String,
    val main: Color,
    val dark: Color,
    val light: Color,
    val ink: Color,
    val bg: Color,
    val border: Color
)

enum class StationTab {
    LISTEN,
    LEARN,
    MEMORIZE,
    QUIZ
}

enum class FontSizeOption(val key: String, val label: String, val spSize: Float) {
    SMALL("sm", "صغير", 20f),
    MEDIUM("md", "متوسط", 24f),
    LARGE("lg", "كبير", 29f);

    companion object {
        fun fromKey(key: String?): FontSizeOption =
            entries.firstOrNull { it.key == key } ?: MEDIUM
    }
}

enum class MemorizeMode {
    LIVE_VOICE,
    PROGRESSIVE_HIDE
}

enum class QuizMode {
    HERO_QUIZ,
    WORD_PUZZLE,
    VOCAB_MATCH
}

data class UserProgress(
    val completedIds: Set<Int> = emptySet(),
    val recitedIds: Set<Int> = emptySet(),
    val puzzleSolvedIds: Set<Int> = emptySet(),
    val vocabSolvedIds: Set<Int> = emptySet(),
    val unlockedUpTo: Int = 1,
    val currentId: Int = 1,
    val teacherMode: Boolean = false,
    val sfxMuted: Boolean = false,
    val fontSize: FontSizeOption = FontSizeOption.MEDIUM,
    val studentName: String = ""
) {
    val heroDisplayName: String
        get() {
            val trimmed = studentName.trim()
            return if (trimmed.length >= 2) trimmed else "بطل"
        }

    fun isUnlocked(hadithId: Int): Boolean {
        if (teacherMode) return true
        return hadithId <= unlockedUpTo || completedIds.contains(hadithId)
    }

    fun frontierId(total: Int = 42): Int {
        for (i in 1..total) {
            if (!completedIds.contains(i) && isUnlocked(i)) return i
        }
        return if (completedIds.size >= total) total else currentId.coerceIn(1, total)
    }
}

object StorybookChapters {
    val chapters: List<ChapterVisual> = listOf(
        ChapterVisual(
            id = 1,
            ord = "الفصل الأول",
            name = "عالم الإخلاص",
            story = "تبدأ حكايتنا هنا: نتعلم أركان ديننا الجميل، وأن نجعل كل أعمالنا خالصةً لله.",
            sceneAsset = "img/scene_1.webp",
            guideAsset = "img/boy_guide.png",
            main = Color(0xFF14B8A6),
            dark = Color(0xFF0F766E),
            light = Color(0xFF5EEAD4),
            ink = Color(0xFF0F766E),
            bg = Color(0xFFF0FDFA),
            border = Color(0xFF99F6E4)
        ),
        ChapterVisual(
            id = 2,
            ord = "الفصل الثاني",
            name = "حديقة الأخلاق",
            story = "ندخل الحديقة لنزرع الرحمة والصدق وحب الخير لكل الناس.",
            sceneAsset = "img/scene_2.webp",
            guideAsset = "img/girl_guide.png",
            main = Color(0xFFF43F5E),
            dark = Color(0xFFBE123C),
            light = Color(0xFFFDA4AF),
            ink = Color(0xFF9F1239),
            bg = Color(0xFFFFF1F2),
            border = Color(0xFFFECDD3)
        ),
        ChapterVisual(
            id = 3,
            ord = "الفصل الثالث",
            name = "واحة الطاعات",
            story = "في الواحة نتعلم الاستقامة، ونكتشف أبواب الخير الكثيرة المفتوحة لنا.",
            sceneAsset = "img/scene_3.webp",
            guideAsset = "img/boy_guide.png",
            main = Color(0xFFF59E0B),
            dark = Color(0xFFB45309),
            light = Color(0xFFFCD34D),
            ink = Color(0xFF92400E),
            bg = Color(0xFFFFFBEB),
            border = Color(0xFFFDE68A)
        ),
        ChapterVisual(
            id = 4,
            ord = "الفصل الرابع",
            name = "سماء الآداب",
            story = "نطير إلى السماء الأخيرة لنتزيّن بآداب المسلم ونفرح بسعة رحمة الله.",
            sceneAsset = "img/scene_4.webp",
            guideAsset = "img/girl_guide.png",
            main = Color(0xFF0EA5E9),
            dark = Color(0xFF0369A1),
            light = Color(0xFF7DD3FC),
            ink = Color(0xFF075985),
            bg = Color(0xFFF0F9FF),
            border = Color(0xFFBAE6FD)
        )
    )

    fun byId(stageId: Int): ChapterVisual =
        chapters.firstOrNull { it.id == stageId } ?: chapters.first()
}

package com.nawawikids.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nawawikids.app.MainActivity
import com.nawawikids.app.R
import com.nawawikids.app.data.SunnahRepository
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.util.ArabicUtils
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

data class ReminderMessage(
    val title: String,
    val body: String,
    val targetHadithId: Int,
    val badgeLabel: String
)

data class ReminderTimePreset(
    val label: String,
    val hour: Int,
    val minute: Int,
    val displayTimeAr: String
)

object ReminderPresets {
    val all: List<ReminderTimePreset> = listOf(
        ReminderTimePreset("صباحاً", 9, 0, "٩:٠٠ ص"),
        ReminderTimePreset("عصراً", 16, 0, "٤:٠٠ م"),
        ReminderTimePreset("مساءً", 19, 0, "٧:٠٠ م"),
        ReminderTimePreset("قبل النوم", 20, 30, "٨:٣٠ م")
    )

    fun formatArabicTime(hour: Int, minute: Int): String {
        val isPm = hour >= 12
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val mm = minute.toString().padStart(2, '0')
        val period = if (isPm) "م" else "ص"
        return "${ArabicUtils.toArabicDigits(h12)}:${ArabicUtils.toArabicDigits(mm)} $period"
    }
}

/**
 * Generates personalized Arabic progress-aware and encouraging daily reminder messages
 * based on the child's saved progress in the 40 Hadiths.
 */
object ReminderMessageComposer {

    fun nextTargetHadith(progress: UserProgress, hadiths: List<Hadith>): Hadith? {
        if (hadiths.isEmpty()) return null
        val sorted = hadiths.sortedBy { it.id }
        // Prioritize the first unlocked Hadith that isn't yet fully memorized & completed
        val nextUnmemorized = sorted.firstOrNull { h ->
            !progress.recitedIds.contains(h.id) && progress.isUnlocked(h.id)
        }
        if (nextUnmemorized != null) return nextUnmemorized

        val nextIncomplete = sorted.firstOrNull { h ->
            !progress.completedIds.contains(h.id) && progress.isUnlocked(h.id)
        }
        if (nextIncomplete != null) return nextIncomplete

        val anyUnmemorized = sorted.firstOrNull { h -> !progress.recitedIds.contains(h.id) }
        return anyUnmemorized ?: sorted.last()
    }

    /**
     * Composes a smart daily notification message.
     * Alternates between direct next-Hadith progress reminders and warm general encouraging reminders.
     */
    fun composeReminder(
        progress: UserProgress,
        hadiths: List<Hadith>,
        daySeed: Int = LocalDate.now().dayOfYear,
        preferProgressMessage: Boolean = false
    ): ReminderMessage {
        val hero = progress.heroDisplayName
        val total = hadiths.size.coerceAtLeast(42)
        val memorizedCount = progress.recitedIds.size
        val completedCount = progress.completedIds.size
        val target = nextTargetHadith(progress, hadiths)
        val targetId = target?.id ?: progress.frontierId(total)
        val targetHeader = target?.header ?: "الحديث ${ArabicUtils.toArabicDigits(targetId)}"
        val targetTitle = target?.title ?: "رحلة الأربعين النووية"

        // All 42 Hadiths memorized — review & celebration reminders
        if (memorizedCount >= total) {
            val reviewHadith = hadiths.getOrNull(daySeed % hadiths.size) ?: target
            val reviewId = reviewHadith?.id ?: 1
            val reviewHeader = reviewHadith?.header ?: "الحديث الأول"
            val reviewTitle = reviewHadith?.title ?: ""
            return ReminderMessage(
                title = "ما شاء الله يا حافظ الأربعين $hero!",
                body = "أتممت حفظ الأربعين النووية! راجع اليوم $reviewHeader ($reviewTitle) ليبقى نور السنة في قلبك.",
                targetHadithId = reviewId,
                badgeLabel = "مراجعة يومية"
            )
        }

        // Progress-specific continuation message
        val progressMessage: ReminderMessage = when {
            memorizedCount == 0 && completedCount == 0 -> {
                ReminderMessage(
                    title = "رحلة الأربعين النووية بانتظارك يا $hero!",
                    body = "ابدأ اليوم بـ $targetHeader ($targetTitle) — استمع وافهم وسمّع لتكسب أول نجمة!",
                    targetHadithId = targetId,
                    badgeLabel = "بداية الرحلة"
                )
            }

            else -> {
                val rec = progress.recordFor(targetId)
                val prevId = (targetId - 1).coerceAtLeast(1)
                val prevHeader = hadiths.firstOrNull { it.id == prevId }?.header
                    ?: "الحديث ${ArabicUtils.toArabicDigits(prevId)}"
                if (rec.listened || rec.learned) {
                    ReminderMessage(
                        title = "$targetHeader بانتظارك يا $hero!",
                        body = "لقد بدأت بالفعل في «$targetTitle» — تبقّت خطوة التسميع والاختبار لتضيء نجمته اليوم!",
                        targetHadithId = targetId,
                        badgeLabel = "إكمال التقدّم"
                    )
                } else {
                    ReminderMessage(
                        title = "$targetHeader بانتظارك يا $hero!",
                        body = "أحسنت في $prevHeader! الآن دور «$targetTitle» — هيا نواصل رحلتنا الجميلة اليوم.",
                        targetHadithId = targetId,
                        badgeLabel = "المحطة التالية"
                    )
                }
            }
        }

        if (preferProgressMessage) return progressMessage

        // Pool of warm general encouraging reminders mixed with next-step nudges
        val remainingForRank = (progress.rank.nextMilestoneStars - completedCount).coerceAtLeast(1)
        val encouragingPool = listOf(
            progressMessage,
            ReminderMessage(
                title = "همّة عالية يا $hero! وقت السنة",
                body = "خمس دقائق فقط اليوم مع $targetHeader ($targetTitle) تزيدك علماً ونوراً وأجراً بإذن الله.",
                targetHadithId = targetId,
                badgeLabel = "تشجيع يومي"
            ),
            ReminderMessage(
                title = "حافظ على سلسلتك يا $hero!",
                body = "سلسلتك اليومية وصلت إلى ${ArabicUtils.toArabicDigits(progress.streakDays)} يوم — افتح $targetHeader الآن لتواصل التألق!",
                targetHadithId = targetId,
                badgeLabel = "سلسلة الأيام"
            ),
            ReminderMessage(
                title = "نجمة جديدة تقرّبك من الترقية يا $hero!",
                body = "تبقّى ${ArabicUtils.toArabicDigits(remainingForRank)} نجمة للوصول إلى رتبة أعلى. $targetHeader بانتظارك الآن!",
                targetHadithId = targetId,
                badgeLabel = "هدف الرتبة"
            ),
            ReminderMessage(
                title = "كل حديث تحفظه كنزٌ في قلبك يا $hero!",
                body = "حفظت حتى الآن ${ArabicUtils.toArabicDigits(memorizedCount)} من ${ArabicUtils.toArabicDigits(total)} حديثاً — هيا نضيف «$targetTitle» إلى إنجازاتك!",
                targetHadithId = targetId,
                badgeLabel = "كنز الحفظ"
            )
        )

        val idx = (daySeed.coerceAtLeast(0)) % encouragingPool.size
        return encouragingPool[idx]
    }
}

object DailyReminderScheduler {
    const val CHANNEL_ID = "nawawi_daily_reminders"
    const val EXTRA_OPEN_HADITH_ID = "extra_open_hadith_id"
    private const val WORK_NAME = "nawawi_kids_daily_reminder_work"
    private const val NOTIFICATION_ID = 4042

    fun ensureNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "تذكيرات حفظ الأربعين النووية",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "تذكيرات يومية تشجيعية لإكمال حفظ ومراجعة أحاديث الأربعين النووية"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun syncSchedule(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        ensureNotificationChannel(context)
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val initialDelayMinutes = computeInitialDelayMinutes(hour, minute)
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .addTag(WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun postNotificationNow(context: Context, message: ReminderMessage): Boolean {
        ensureNotificationChannel(context)
        if (!hasNotificationPermission(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_HADITH_ID, message.targetHadithId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            message.targetHadithId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun computeInitialDelayMinutes(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var target = now.withHour(hour.coerceIn(0, 23))
            .withMinute(minute.coerceIn(0, 59))
            .withSecond(0)
            .withNano(0)
        if (!target.isAfter(now.plusMinutes(1))) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target).toMinutes().coerceAtLeast(15L)
    }
}

/**
 * Periodic WorkManager job that runs daily, reads the student's latest saved progress,
 * and posts a personalized Arabic continuation or encouraging notification.
 */
class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repo = SunnahRepository(applicationContext)
            val progress = repo.progressFlow.first()
            if (!progress.dailyReminderEnabled) {
                return Result.success()
            }
            val dataset = repo.loadBookDataset("nawawi_40")
            val message = ReminderMessageComposer.composeReminder(
                progress = progress,
                hadiths = dataset.hadiths,
                daySeed = LocalDate.now().dayOfYear
            )
            DailyReminderScheduler.postNotificationNow(applicationContext, message)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

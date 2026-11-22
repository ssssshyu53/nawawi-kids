package com.nawawikids.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.model.ChapterVisual
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.model.HadithDataset
import com.nawawikids.app.model.HadithProgressRecord
import com.nawawikids.app.model.StorybookChapters
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.notifications.DailyReminderScheduler
import com.nawawikids.app.notifications.ReminderMessageComposer
import com.nawawikids.app.notifications.ReminderPresets
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.RibbonBadge
import com.nawawikids.app.ui.components.StorybookBackground
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils
import kotlin.math.roundToInt

private enum class DashboardFilter(val label: String) {
    ALL("الكل"),
    MEMORIZED("محفوظة"),
    IN_PROGRESS("قيد الحفظ"),
    NOT_STARTED("لم تبدأ")
}

/**
 * Full-screen Progress Tracking Dashboard:
 * - Hero overall memorization circular gauge + accuracy, stars & streak stats
 * - Automatic Daily Reminders configuration card with live personalized message preview
 * - Chapter-grouped list/grid displaying a crisp animated Circular Progress Indicator
 *   and 4-step breakdown for EVERY single Hadith (1..42)
 */
@Composable
fun ProgressDashboardScreen(
    dataset: HadithDataset,
    progress: UserProgress,
    onBack: () -> Unit,
    onOpenStation: (Int) -> Unit,
    onUpdateDailyReminder: (Boolean, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val hadiths = dataset.hadiths
    val total = hadiths.size.coerceAtLeast(42)
    val memorizedCount = progress.recitedIds.size
    val completedCount = progress.completedIds.size
    val frontierId = progress.frontierId(total)

    val avgAccuracy = remember(progress.hadithRecords) {
        val scores = progress.hadithRecords.values
            .map { it.bestRecitationAccuracy }
            .filter { it > 0 }
        if (scores.isEmpty()) 0 else scores.average().roundToInt()
    }

    var activeFilter by remember { mutableStateOf(DashboardFilter.ALL) }
    var selectedWorldFilter by remember { mutableIntStateOf(0) } // 0 = all worlds
    var previewDayOffset by remember { mutableIntStateOf(0) }
    var reminderStatusBanner by remember { mutableStateOf<String?>(null) }
    var pendingActionAfterPermission by remember { mutableStateOf<(() -> Unit)?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            reminderStatusBanner = "تم تفعيل إذن الإشعارات بنجاح!"
            pendingActionAfterPermission?.invoke()
        } else {
            reminderStatusBanner = "لم يتم منح إذن الإشعارات — يمكنك تفعيله لاحقاً من إعدادات الهاتف."
        }
        pendingActionAfterPermission = null
    }

    fun runWithNotificationPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !DailyReminderScheduler.hasNotificationPermission(context)
        ) {
            pendingActionAfterPermission = action
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    val filteredHadiths = remember(hadiths, activeFilter, selectedWorldFilter, progress) {
        hadiths.filter { h ->
            val worldOk = selectedWorldFilter == 0 || h.stage == selectedWorldFilter
            val rec = progress.recordFor(h.id)
            val statusOk = when (activeFilter) {
                DashboardFilter.ALL -> true
                DashboardFilter.MEMORIZED -> rec.memorized || progress.recitedIds.contains(h.id)
                DashboardFilter.IN_PROGRESS -> !rec.memorized && (rec.progressFraction > 0f)
                DashboardFilter.NOT_STARTED -> rec.progressFraction == 0f
            }
            worldOk && statusOk
        }
    }

    // Group filtered Hadiths cleanly by chapter so chapters never collide on the same row
    val groupedByChapter = remember(filteredHadiths) {
        StorybookChapters.chapters.mapNotNull { ch ->
            val list = filteredHadiths.filter { it.stage == ch.id }
            if (list.isEmpty()) null else ch to list
        }
    }

    val statusPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    StorybookBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = statusPadding)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CandyButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("dashboard_back_button"),
                        topColor = Color(0xFF38BDF8),
                        bottomColor = Color(0xFF0284C7),
                        borderColor = StorybookColors.SkyDark,
                        lipColor = Color(0xFF075985),
                        cornerRadius = 15.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "العودة للخريطة",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "رجوع",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لوحة التقدّم والحفظ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.StoryNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "تقدّم ${progress.heroDisplayName} في حفظ الأربعين النووية",
                            style = MaterialTheme.typography.labelSmall,
                            color = StorybookColors.StoryMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = StorybookColors.EmeraldSoft,
                        border = BorderStroke(1.5.dp, StorybookColors.EmeraldMain)
                    ) {
                        Text(
                            text = "${ArabicUtils.toArabicDigits(memorizedCount)}/${ArabicUtils.toArabicDigits(total)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.EmeraldDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                val useTwoColumns = maxWidth >= 600.dp

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 14.dp,
                        bottom = 28.dp + navPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Hero Overall Memorization Dashboard Card
                    item(key = "hero_memorization_card") {
                        OverallMemorizationHeroCard(
                            progress = progress,
                            hadiths = hadiths,
                            memorizedCount = memorizedCount,
                            completedCount = completedCount,
                            avgAccuracy = avgAccuracy,
                            frontierId = frontierId,
                            onContinueNext = { onOpenStation(frontierId) }
                        )
                    }

                    // 2. Daily Automatic Reminders Card (clean, without test button)
                    item(key = "daily_reminders_card") {
                        DailyRemindersCard(
                            progress = progress,
                            hadiths = hadiths,
                            previewDayOffset = previewDayOffset,
                            statusBanner = reminderStatusBanner,
                            onCyclePreview = { previewDayOffset = (previewDayOffset + 1) % 5 },
                            onToggleReminder = { enabled ->
                                if (enabled) {
                                    runWithNotificationPermission {
                                        onUpdateDailyReminder(
                                            true,
                                            progress.reminderHour,
                                            progress.reminderMinute
                                        )
                                        reminderStatusBanner =
                                            "تم تفعيل التذكير اليومي الساعة ${ReminderPresets.formatArabicTime(progress.reminderHour, progress.reminderMinute)}"
                                    }
                                } else {
                                    onUpdateDailyReminder(
                                        false,
                                        progress.reminderHour,
                                        progress.reminderMinute
                                    )
                                    reminderStatusBanner = "تم إيقاف التذكير اليومي مؤقتاً."
                                }
                            },
                            onSelectTime = { hour, minute ->
                                runWithNotificationPermission {
                                    onUpdateDailyReminder(true, hour, minute)
                                    reminderStatusBanner =
                                        "تم ضبط وقت التذكير اليومي على ${ReminderPresets.formatArabicTime(hour, minute)}"
                                }
                            }
                        )
                    }

                    // 3. Per-Hadith Circular Progress Section Header + Filters
                    item(key = "hadith_rings_header") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "تقدّمك في كل حديث (${ArabicUtils.toArabicDigits(filteredHadiths.size)})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = StorybookColors.StoryNavy
                                    )
                                    Text(
                                        text = "اضغط على أي حديث مفتوح لإكمال الاستماع والفهم والتسميع",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StorybookColors.StoryMuted
                                    )
                                }
                            }

                            // Status Filters
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                items(DashboardFilter.entries) { filter ->
                                    val selected = filter == activeFilter
                                    Surface(
                                        shape = CircleShape,
                                        color = if (selected) StorybookColors.TealMain else Color.White,
                                        border = BorderStroke(
                                            width = 1.5.dp,
                                            color = if (selected) StorybookColors.TealDark else StorybookColors.GoldBorder
                                        ),
                                        modifier = Modifier
                                            .clickable { activeFilter = filter }
                                            .testTag("dashboard_filter_${filter.name}")
                                    ) {
                                        Text(
                                            text = filter.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (selected) Color.White else StorybookColors.StoryNavy,
                                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            // World Filters
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item(key = "world_all") {
                                    val sel = selectedWorldFilter == 0
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (sel) StorybookColors.StoryNavy else Color.White,
                                        border = BorderStroke(1.dp, if (sel) StorybookColors.StoryNavy else StorybookColors.GoldBorder),
                                        modifier = Modifier
                                            .clickable { selectedWorldFilter = 0 }
                                            .testTag("dashboard_world_0")
                                    ) {
                                        Text(
                                            text = "كل العوالم",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) Color.White else StorybookColors.StoryMuted,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                                items(StorybookChapters.chapters, key = { "wf_${it.id}" }) { ch ->
                                    val sel = selectedWorldFilter == ch.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (sel) ch.main else ch.bg,
                                        border = BorderStroke(1.dp, if (sel) ch.dark else ch.border),
                                        modifier = Modifier
                                            .clickable { selectedWorldFilter = ch.id }
                                            .testTag("dashboard_world_${ch.id}")
                                    ) {
                                        Text(
                                            text = ch.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) Color.White else ch.ink,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Clean Chapter-Grouped Hadith Circular Progress Cards
                    if (groupedByChapter.isEmpty()) {
                        item(key = "dashboard_empty_state") {
                            ToyCard(containerColor = Color.White) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = StorybookColors.GoldMain,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Text(
                                        text = "لا توجد أحاديث في هذا التصنيف بعد",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = StorybookColors.StoryNavy
                                    )
                                    Text(
                                        text = "جرّب عرض كل الأحاديث أو ابدأ بحفظ الحديث التالي الآن!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StorybookColors.StoryMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        groupedByChapter.forEach { (chapter, chapterHadiths) ->
                            // Crisp Chapter Section Header
                            item(key = "chapter_header_${chapter.id}") {
                                val chapterMem = chapterHadiths.count { progress.recitedIds.contains(it.id) }
                                ChapterSectionHeader(
                                    chapter = chapter,
                                    memorizedInChapter = chapterMem,
                                    totalInChapter = chapterHadiths.size
                                )
                            }

                            if (useTwoColumns) {
                                val rows = chapterHadiths.chunked(2)
                                items(
                                    items = rows,
                                    key = { row -> "ch${chapter.id}_row_${row.first().id}" }
                                ) { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { hadith ->
                                            HadithProgressRowCard(
                                                hadith = hadith,
                                                world = chapter,
                                                record = progress.recordFor(hadith.id),
                                                isUnlocked = progress.isUnlocked(hadith.id),
                                                isMemorized = progress.recitedIds.contains(hadith.id),
                                                isCompleted = progress.completedIds.contains(hadith.id),
                                                isFrontier = hadith.id == frontierId,
                                                onClick = {
                                                    if (progress.isUnlocked(hadith.id)) {
                                                        onOpenStation(hadith.id)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                items(
                                    items = chapterHadiths,
                                    key = { h -> "hadith_card_${h.id}" }
                                ) { hadith ->
                                    HadithProgressRowCard(
                                        hadith = hadith,
                                        world = chapter,
                                        record = progress.recordFor(hadith.id),
                                        isUnlocked = progress.isUnlocked(hadith.id),
                                        isMemorized = progress.recitedIds.contains(hadith.id),
                                        isCompleted = progress.completedIds.contains(hadith.id),
                                        isFrontier = hadith.id == frontierId,
                                        onClick = {
                                            if (progress.isUnlocked(hadith.id)) {
                                                onOpenStation(hadith.id)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
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

@Composable
private fun ChapterSectionHeader(
    chapter: ChapterVisual,
    memorizedInChapter: Int,
    totalInChapter: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(22.dp)
                .clip(CircleShape)
                .background(chapter.main)
        )
        Text(
            text = "${chapter.ord}: ${chapter.name}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = chapter.ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Surface(
            shape = CircleShape,
            color = chapter.bg,
            border = BorderStroke(1.dp, chapter.main.copy(alpha = 0.45f))
        ) {
            Text(
                text = "محفوظ ${ArabicUtils.toArabicDigits(memorizedInChapter)}/${ArabicUtils.toArabicDigits(totalInChapter)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = chapter.ink,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Clean, legible horizontal Hadith card with a high-contrast Circular Progress Indicator,
 * full untruncated Arabic title, 4 visual step chips (استمع • افهم • سمّع • العب),
 * and a crisp status/percentage badge.
 */
@Composable
private fun HadithProgressRowCard(
    hadith: Hadith,
    world: ChapterVisual,
    record: HadithProgressRecord,
    isUnlocked: Boolean,
    isMemorized: Boolean,
    isCompleted: Boolean,
    isFrontier: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = record.progressFraction
    val pct = (fraction * 100f).roundToInt()

    val cardBg = when {
        isMemorized && isCompleted -> Color(0xFFF0FDF4)
        isFrontier -> Color(0xFFFFFBEB)
        !isUnlocked -> Color(0xFFF8FAFC)
        else -> Color.White
    }
    val cardBorder = when {
        isMemorized && isCompleted -> StorybookColors.EmeraldMain
        isFrontier -> StorybookColors.GoldMain
        !isUnlocked -> Color(0xFFCBD5E1)
        else -> world.main.copy(alpha = 0.35f)
    }
    val ringColors = when {
        isMemorized && isCompleted -> listOf(StorybookColors.EmeraldMain, StorybookColors.TealMain)
        isMemorized -> listOf(StorybookColors.VioletMain, StorybookColors.TealMain)
        isFrontier -> listOf(StorybookColors.GoldMain, world.main)
        else -> listOf(world.main, world.dark)
    }
    val trackColor = when {
        !isUnlocked -> Color(0xFFE2E8F0)
        isMemorized && isCompleted -> StorybookColors.EmeraldMain.copy(alpha = 0.18f)
        else -> world.main.copy(alpha = 0.16f)
    }

    val statusLabel = when {
        isMemorized && isCompleted -> "متقَن"
        isMemorized -> "محفوظ"
        isFrontier -> "المحطة الحالية"
        fraction > 0f -> "${ArabicUtils.toArabicDigits(pct)}٪"
        !isUnlocked -> "مغلق"
        else -> "جاهز"
    }
    val statusBg = when {
        isMemorized -> StorybookColors.EmeraldSoft
        isFrontier -> StorybookColors.GoldSoft
        fraction > 0f -> world.bg
        else -> Color(0xFFF1F5F9)
    }
    val statusInk = when {
        isMemorized -> StorybookColors.EmeraldDark
        isFrontier -> StorybookColors.GoldDeep
        fraction > 0f -> world.ink
        else -> StorybookColors.LockedInk
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(if (isFrontier || (isMemorized && isCompleted)) 2.dp else 1.25.dp, cardBorder),
        shadowElevation = if (isUnlocked) 2.dp else 0.dp,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("dashboard_hadith_ring_${hadith.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // High-Contrast Circular Progress Indicator
            AnimatedCircularProgressRing(
                progress = fraction,
                size = 56.dp,
                strokeWidth = 5.5.dp,
                trackColor = trackColor,
                activeColors = ringColors
            ) {
                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "مغلق",
                        tint = StorybookColors.LockedInk,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isMemorized && isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(StorybookColors.EmeraldSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "مكتمل",
                            tint = StorybookColors.EmeraldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Text(
                        text = ArabicUtils.toArabicDigits(hadith.id),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isFrontier) StorybookColors.GoldDeep else world.ink
                    )
                }
            }

            // Center: Header + Full Title + 4 Step Pills
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = hadith.header,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUnlocked) world.dark else StorybookColors.LockedInk
                    )
                    Surface(
                        shape = CircleShape,
                        color = statusBg,
                        border = BorderStroke(1.dp, statusInk.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = statusInk,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.5.dp),
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = hadith.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isUnlocked) StorybookColors.StoryNavy else StorybookColors.LockedInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // 4 Mini Step Indicators showing exact breakdown of the ring
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DashboardMiniStepBadge("استمع", record.listened, StorybookColors.TealMain, StorybookColors.TealSoft, StorybookColors.TealDark)
                    DashboardMiniStepBadge("افهم", record.learned, StorybookColors.GoldMain, StorybookColors.GoldSoft, StorybookColors.GoldDeep)
                    DashboardMiniStepBadge("سمّع", record.memorized, StorybookColors.VioletMain, StorybookColors.VioletSoft, StorybookColors.VioletDark)
                    DashboardMiniStepBadge("العب", record.quizCompleted, StorybookColors.CoralMain, StorybookColors.CoralSoft, StorybookColors.CoralDark)
                }
            }

            // Trailing Navigation Chevron (only for unlocked Hadiths)
            if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(world.bg)
                        .border(1.dp, world.main.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "فتح ${hadith.header}",
                        tint = world.dark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardMiniStepBadge(
    label: String,
    done: Boolean,
    accent: Color,
    softBg: Color,
    ink: Color
) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = if (done) softBg else Color(0xFFF1F5F9),
        border = BorderStroke(
            width = 1.dp,
            color = if (done) accent.copy(alpha = 0.65f) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (done) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = ink,
                    modifier = Modifier.size(10.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = if (done) FontWeight.Black else FontWeight.Medium,
                color = if (done) ink else StorybookColors.LockedInk,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OverallMemorizationHeroCard(
    progress: UserProgress,
    hadiths: List<Hadith>,
    memorizedCount: Int,
    completedCount: Int,
    avgAccuracy: Int,
    frontierId: Int,
    onContinueNext: () -> Unit
) {
    val total = hadiths.size.coerceAtLeast(42)
    val memorizedFraction = (memorizedCount.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val overallPct = (memorizedFraction * 100f).roundToInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E2A47), RoundedCornerShape(28.dp))
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1E3A5F), Color(0xFF0F766E))
                )
            )
            .border(2.dp, Color(0xFF5EEAD4).copy(alpha = 0.45f), RoundedCornerShape(28.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Master Circular Progress Gauge
                AnimatedCircularProgressRing(
                    progress = memorizedFraction,
                    size = 114.dp,
                    strokeWidth = 11.dp,
                    trackColor = Color.White.copy(alpha = 0.16f),
                    activeColors = listOf(StorybookColors.GoldLight, StorybookColors.EmeraldMain, Color(0xFF5EEAD4)),
                    modifier = Modifier.testTag("dashboard_master_ring")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${ArabicUtils.toArabicDigits(memorizedCount)}/${ArabicUtils.toArabicDigits(total)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "محفوظة",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StorybookColors.GoldLight
                        )
                        Text(
                            text = "${ArabicUtils.toArabicDigits(overallPct)}٪",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCCFBF1)
                        )
                    }
                }

                // Key Metrics Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "إنجاز الحفظ الكلي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.GoldLight
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardHeroStatPill(
                            icon = Icons.Rounded.Star,
                            label = "النجوم",
                            value = "${ArabicUtils.toArabicDigits(completedCount)}",
                            tint = StorybookColors.GoldLight,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardHeroStatPill(
                            icon = Icons.Rounded.LocalFireDepartment,
                            label = "السلسلة",
                            value = "${ArabicUtils.toArabicDigits(progress.streakDays)} يوم",
                            tint = Color(0xFFFDA4AF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardHeroStatPill(
                            icon = Icons.Rounded.Mic,
                            label = "متوسط الدقة",
                            value = if (avgAccuracy > 0) "${ArabicUtils.toArabicDigits(avgAccuracy)}٪" else "—",
                            tint = Color(0xFFC4B5FD),
                            modifier = Modifier.weight(1f)
                        )
                        DashboardHeroStatPill(
                            icon = Icons.Rounded.AutoAwesome,
                            label = "الرتبة",
                            value = progress.rank.title,
                            tint = Color(0xFF5EEAD4),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4-World Mini Progress Breakdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorybookChapters.chapters.forEach { chapter ->
                    val chIds = hadiths.filter { it.stage == chapter.id }.map { it.id }
                    val chMem = chIds.count { progress.recitedIds.contains(it) }
                    val frac = if (chIds.isEmpty()) 0f else chMem.toFloat() / chIds.size.toFloat()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = chapter.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.width(86.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(frac.coerceAtLeast(if (chMem > 0) 0.06f else 0f))
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(chapter.light)
                            )
                        }
                        Text(
                            text = "${ArabicUtils.toArabicDigits(chMem)}/${ArabicUtils.toArabicDigits(chIds.size)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = StorybookColors.GoldLight,
                            modifier = Modifier.width(34.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Quick Continue CTA
            val nextHadith = hadiths.firstOrNull { it.id == frontierId }
            CandyButton(
                onClick = onContinueNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_continue_next_button"),
                topColor = Color(0xFFFDE047),
                bottomColor = StorybookColors.GoldMain,
                borderColor = Color(0xFFFDE047),
                lipColor = StorybookColors.GoldDark,
                contentColor = Color(0xFF451A03),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF451A03), modifier = Modifier.size(20.dp))
                Text(
                    text = if (nextHadith != null) {
                        "واصل الحفظ: ${nextHadith.header} (${nextHadith.title})"
                    } else {
                        "واصل رحلتك الآن"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF451A03),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DashboardHeroStatPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE0F2FE),
                fontSize = 10.sp,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DailyRemindersCard(
    progress: UserProgress,
    hadiths: List<Hadith>,
    previewDayOffset: Int,
    statusBanner: String?,
    onCyclePreview: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSelectTime: (Int, Int) -> Unit
) {
    val previewMessage = remember(progress, hadiths, previewDayOffset) {
        ReminderMessageComposer.composeReminder(
            progress = progress,
            hadiths = hadiths,
            daySeed = previewDayOffset,
            preferProgressMessage = previewDayOffset == 0
        )
    }

    ToyCard(
        containerColor = Color(0xFFFFFDF7),
        borderColor = StorybookColors.GoldBorder,
        lipColor = Color(0xFFFDE68A),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(15.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header + Master Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (progress.dailyReminderEnabled) StorybookColors.TealSoft else Color(0xFFF1F5F9))
                            .border(
                                1.5.dp,
                                if (progress.dailyReminderEnabled) StorybookColors.TealMain else Color(0xFFCBD5E1),
                                RoundedCornerShape(13.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (progress.dailyReminderEnabled) {
                                Icons.Rounded.NotificationsActive
                            } else {
                                Icons.Rounded.NotificationsOff
                            },
                            contentDescription = null,
                            tint = if (progress.dailyReminderEnabled) StorybookColors.TealDark else StorybookColors.LockedInk,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "التذكيرات اليومية التلقائية",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.StoryNavy
                        )
                        Text(
                            text = if (progress.dailyReminderEnabled) {
                                "مفعّلة يومياً • الساعة ${ReminderPresets.formatArabicTime(progress.reminderHour, progress.reminderMinute)}"
                            } else {
                                "فعّلها ليذكّرك التطبيق يومياً بإكمال حفظك"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (progress.dailyReminderEnabled) StorybookColors.TealDark else StorybookColors.StoryMuted
                        )
                    }
                }

                Switch(
                    checked = progress.dailyReminderEnabled,
                    onCheckedChange = onToggleReminder,
                    modifier = Modifier.testTag("daily_reminder_switch"),
                    colors = SwitchDefaults.colors(checkedTrackColor = StorybookColors.TealMain)
                )
            }

            // Preferred Time Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = StorybookColors.GoldDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "اختر وقت التذكير المناسب يومياً:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StorybookColors.StoryInk
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReminderPresets.all.forEach { preset ->
                        val selected = progress.dailyReminderEnabled &&
                            progress.reminderHour == preset.hour &&
                            progress.reminderMinute == preset.minute
                        Surface(
                            shape = RoundedCornerShape(13.dp),
                            color = if (selected) StorybookColors.TealMain else Color.White,
                            border = BorderStroke(
                                1.5.dp,
                                if (selected) StorybookColors.TealDark else StorybookColors.GoldBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(13.dp))
                                .clickable { onSelectTime(preset.hour, preset.minute) }
                                .testTag("reminder_time_${preset.hour}_${preset.minute}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) Color.White else StorybookColors.StoryNavy,
                                    maxLines = 1
                                )
                                Text(
                                    text = preset.displayTimeAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (selected) Color(0xFFCCFBF1) else StorybookColors.StoryMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Live Smart Reminder Preview Bubble
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = StorybookColors.TealSoft.copy(alpha = 0.55f),
                border = BorderStroke(1.5.dp, StorybookColors.TealBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCyclePreview)
                    .testTag("reminder_preview_card")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RibbonBadge(
                            text = "معاينة: ${previewMessage.badgeLabel}",
                            topColor = StorybookColors.TealMain,
                            bottomColor = StorybookColors.TealDark,
                            borderColor = Color(0xFF115E59)
                        )
                        Text(
                            text = "اضغط لرسالة أخرى",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StorybookColors.TealDark
                        )
                    }
                    Text(
                        text = previewMessage.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.StoryNavy
                    )
                    Text(
                        text = previewMessage.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = StorybookColors.StoryInk
                    )
                }
            }

            if (!statusBanner.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StorybookColors.GoldSoft,
                    border = BorderStroke(1.dp, StorybookColors.GoldMain),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusBanner,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StorybookColors.GoldDeep,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Reusable smooth Canvas circular progress ring with rounded stroke caps & gradient sweep.
 */
@Composable
fun AnimatedCircularProgressRing(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    trackColor: Color,
    activeColors: List<Color>,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "circular_progress_sweep"
    )
    val gradientColors = if (activeColors.size >= 2) activeColors else listOf(
        activeColors.firstOrNull() ?: StorybookColors.TealMain,
        activeColors.firstOrNull() ?: StorybookColors.TealMain
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)

            // Background Track Circle
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Progress Arc
            if (animatedProgress > 0.005f) {
                drawArc(
                    brush = Brush.sweepGradient(gradientColors),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        centerContent()
    }
}

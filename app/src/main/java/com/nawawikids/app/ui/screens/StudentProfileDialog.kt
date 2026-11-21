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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nawawikids.app.model.AvatarChoice
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.RibbonBadge
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils

/**
 * Student Hero Passport Dialog: saves student profile (name, age, companion avatar)
 * and displays rich saved stats (stars, XP, daily streak, recited hadiths, games won, rank)
 * plus Parent/Teacher & Audio settings.
 */
@Composable
fun StudentProfileDialog(
    progress: UserProgress,
    totalHadiths: Int,
    onSaveProfile: (String, Int?, AvatarChoice) -> Unit,
    onToggleTeacherMode: (Boolean) -> Unit,
    onToggleSfx: () -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember(progress.studentName) { mutableStateOf(progress.studentName) }
    var ageText by remember(progress.studentAge) { mutableStateOf(progress.studentAge?.toString() ?: "") }
    var avatarInput by remember(progress.avatarChoice) { mutableStateOf(progress.avatarChoice) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val gamesWonCount = progress.puzzleSolvedIds.size + progress.vocabSolvedIds.size
    val rank = progress.rank
    val rankProgress = (progress.completedIds.size.toFloat() / rank.nextMilestoneStars.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = StorybookColors.CreamBg,
            border = BorderStroke(3.dp, StorybookColors.GoldMain),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RibbonBadge(
                        text = "ملف البطل وتقدّمك المحفوظ",
                        topColor = StorybookColors.TealMain,
                        bottomColor = StorybookColors.TealDark,
                        borderColor = Color(0xFF115E59)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_profile_dialog")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = StorybookColors.CoralDark)
                    }
                }

                // Rank & XP Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(StorybookColors.StoryNavy, Color(0xFF0F766E))
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = StorybookColors.GoldLight,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "الرتبة الحالية",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFBAE6FD)
                                    )
                                    Text(
                                        text = rank.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = StorybookColors.GoldMain
                            ) {
                                Text(
                                    text = "${ArabicUtils.toArabicDigits(progress.totalXp)} نقطة",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = StorybookColors.StoryInk,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(rankProgress.coerceAtLeast(0.05f))
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(StorybookColors.GoldLight)
                            )
                        }
                        Text(
                            text = "النجوم: ${ArabicUtils.toArabicDigits(progress.completedIds.size)} من ${ArabicUtils.toArabicDigits(rank.nextMilestoneStars)} للرتبة التالية",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE0F2FE)
                        )
                    }
                }

                // 4 Saved Stat Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMiniTile(
                        icon = Icons.Rounded.Star,
                        value = "${ArabicUtils.toArabicDigits(progress.completedIds.size)}/${ArabicUtils.toArabicDigits(totalHadiths)}",
                        label = "نجوم",
                        bg = StorybookColors.GoldSoft,
                        tint = StorybookColors.GoldDark,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniTile(
                        icon = Icons.Rounded.LocalFireDepartment,
                        value = ArabicUtils.toArabicDigits(progress.streakDays),
                        label = "أيام متتالية",
                        bg = StorybookColors.CoralSoft,
                        tint = StorybookColors.CoralDark,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniTile(
                        icon = Icons.Rounded.Mic,
                        value = ArabicUtils.toArabicDigits(progress.recitedIds.size),
                        label = "تسميع صوتي",
                        bg = StorybookColors.VioletSoft,
                        tint = StorybookColors.VioletDark,
                        modifier = Modifier.weight(1f)
                    )
                    StatMiniTile(
                        icon = Icons.Rounded.Extension,
                        value = ArabicUtils.toArabicDigits(gamesWonCount),
                        label = "ألعاب",
                        bg = StorybookColors.TealSoft,
                        tint = StorybookColors.TealDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Choose Companion Avatar
                Text(
                    text = "اختر شخصيتك المفضلة في الرحلة:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = StorybookColors.StoryNavy
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarChoice.entries.forEach { choice ->
                        val selected = avatarInput == choice
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) StorybookColors.TealSoft else Color.White,
                            border = BorderStroke(
                                width = if (selected) 3.dp else 1.5.dp,
                                color = if (selected) StorybookColors.TealMain else StorybookColors.GoldBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { avatarInput = choice }
                                .testTag("avatar_choice_${choice.key}")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AssetImage(
                                    assetPath = choice.assetPath,
                                    contentDescription = choice.displayName,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Text(
                                    text = choice.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) StorybookColors.TealDark else StorybookColors.StoryInk,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(StorybookColors.TealMain),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Student Name Input
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(40) },
                    label = { Text("اسم البطل (يظهر على الشهادة)") },
                    placeholder = { Text("اكتب اسمك هنا") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StorybookColors.TealMain,
                        unfocusedBorderColor = StorybookColors.GoldBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )

                // Open Student Age Input (no fixed age restriction)
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { raw -> ageText = normalizeDigitsOnly(raw).take(3) },
                    label = { Text("عمر البطل بالسنوات (اختياري)") },
                    placeholder = { Text("اكتب العمر بحرية") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StorybookColors.TealMain,
                        unfocusedBorderColor = StorybookColors.GoldBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_age_input")
                )

                // Save Profile CTA
                CandyButton(
                    onClick = {
                        onSaveProfile(nameInput, parseOpenAge(ageText), avatarInput)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_profile_button"),
                    topColor = Color(0xFF34D399),
                    bottomColor = Color(0xFF059669),
                    borderColor = Color(0xFF047857),
                    lipColor = Color(0xFF064E3B),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                    Text("حفظ بيانات البطل", fontWeight = FontWeight.Black, color = Color.White)
                }

                // Settings Section: SFX + Teacher Mode + Reset
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, StorybookColors.GoldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (progress.sfxMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = StorybookColors.SkyDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "المؤثرات الصوتية التشجيعية",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.StoryInk
                                )
                            }
                            Switch(
                                checked = !progress.sfxMuted,
                                onCheckedChange = { onToggleSfx() },
                                colors = SwitchDefaults.colors(checkedTrackColor = StorybookColors.TealMain)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = StorybookColors.GoldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "وضع المعلم / ولي الأمر (فتح الكل)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.StoryInk
                                )
                            }
                            Switch(
                                checked = progress.teacherMode,
                                onCheckedChange = onToggleTeacherMode,
                                modifier = Modifier.testTag("profile_teacher_switch"),
                                colors = SwitchDefaults.colors(checkedTrackColor = StorybookColors.SkyAccent)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showResetConfirm = true },
                                modifier = Modifier.testTag("profile_reset_button")
                            ) {
                                Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = StorybookColors.CoralDark, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("إعادة ضبط التقدّم", color = StorybookColors.CoralDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = StorybookColors.CreamBg,
            title = { Text("إعادة بدء الرحلة؟", fontWeight = FontWeight.Black, color = StorybookColors.StoryNavy) },
            text = { Text("سيتم مسح جميع النجوم المكتسبة والبدء من الحديث الأول مع الاحتفاظ باسمك وبياناتك.", color = StorybookColors.StoryInk) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirm = false
                        onResetProgress()
                        onDismiss()
                    }
                ) {
                    Text("نعم، أعد البداية", color = StorybookColors.CoralDark, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("إلغاء", color = StorybookColors.TealDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun StatMiniTile(
    icon: ImageVector,
    value: String,
    label: String,
    bg: Color,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.5.dp, tint.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = StorybookColors.StoryInk
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                maxLines = 1
            )
        }
    }
}

package com.nawawikids.app.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nawawikids.app.model.UserProgress
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.RibbonBadge
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils
import com.nawawikids.app.util.CertificateRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CertificateDialog(
    progress: UserProgress,
    totalHadiths: Int,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val recitedCount = progress.recitedIds.size
    // Certificate strictly requires completing live recitation of ALL Hadiths —
    // Free Browse Mode ("وضع التصفح الحر") NEVER bypasses this requirement.
    val canIssue = recitedCount >= totalHadiths
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(progress.studentName, canIssue) {
        if (canIssue) {
            renderedBitmap = withContext(Dispatchers.IO) {
                CertificateRenderer.renderCertificateBitmap(
                    context = context,
                    rawStudentName = progress.studentName
                )
            }
        }
    }

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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RibbonBadge(text = "شهادة بطل الأربعين النووية")
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_cert_dialog_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = StorybookColors.CoralDark)
                    }
                }

                if (!canIssue) {
                    val left = (totalHadiths - recitedCount).coerceAtLeast(0)
                    val pct = if (totalHadiths > 0) (recitedCount.toFloat() / totalHadiths.toFloat()).coerceIn(0f, 1f) else 0f

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(StorybookColors.GoldSoft)
                            .border(2.5.dp, StorybookColors.GoldMain, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = StorybookColors.GoldDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "أتممت تسميع ${ArabicUtils.toArabicDigits(recitedCount)} من ${ArabicUtils.toArabicDigits(totalHadiths)} حديثاً",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.StoryNavy,
                        textAlign = TextAlign.Center
                    )

                    LinearProgressIndicator(
                        progress = { pct },
                        color = StorybookColors.TealMain,
                        trackColor = StorybookColors.GoldBorder.copy(alpha = 0.45f),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Text(
                        text = "بقي لك إتمام تسميع ${ArabicUtils.toArabicDigits(left)} حديثاً لتُفتح شهادتك الرسمية باسمك من منصة أبطال السنة!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StorybookColors.StoryMuted,
                        textAlign = TextAlign.Center
                    )

                    if (progress.teacherMode) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = StorybookColors.SkySoft,
                            border = BorderStroke(1.5.dp, StorybookColors.SkyAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Explore,
                                    contentDescription = null,
                                    tint = StorybookColors.SkyDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "وضع التصفح الحر يفتح الأحاديث للاستعراض فقط، ولا يمكن إصدار الشهادة إلا بعد إتمام تسميع جميع الأحاديث الـ ${ArabicUtils.toArabicDigits(totalHadiths)}.",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StorybookColors.SkyDark
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "اكتب اسم البطل كما تحب أن يظهر على الشهادة:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = StorybookColors.SkyDark,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = progress.studentName,
                        onValueChange = onNameChange,
                        placeholder = { Text("الطالب المتفوق") },
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
                            .testTag("certificate_name_input")
                    )

                    val bmp = renderedBitmap
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "شهادة التقدير",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(CertificateRenderer.TEMPLATE_ASPECT_RATIO)
                                .clip(RoundedCornerShape(18.dp))
                                .border(3.dp, StorybookColors.GoldMain, RoundedCornerShape(18.dp))
                        )

                        CandyButton(
                            onClick = {
                                val uri = CertificateRenderer.saveAndGetShareUri(context, bmp)
                                if (uri != null) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "شهادة إتمام الأربعين النووية — منصة أبطال السنة — ${progress.heroDisplayName}"
                                        )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة شهادة البطل"))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_certificate_button"),
                            topColor = Color(0xFF34D399),
                            bottomColor = Color(0xFF059669),
                            borderColor = Color(0xFF047857),
                            lipColor = Color(0xFF064E3B),
                            contentPadding = PaddingValues(vertical = 13.dp)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("حفظ ومشاركة الشهادة", fontWeight = FontWeight.Black, color = Color.White)
                        }
                    } else {
                        Spacer(Modifier.height(24.dp))
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = StorybookColors.GoldMain,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("نجهّز شهادتك الذهبية...", color = StorybookColors.GoldDeep)
                    }
                }
            }
        }
    }
}

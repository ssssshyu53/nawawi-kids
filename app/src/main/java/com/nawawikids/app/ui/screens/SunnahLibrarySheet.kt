package com.nawawikids.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nawawikids.app.model.SunnahBook
import com.nawawikids.app.ui.components.RibbonBadge
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils

/**
 * Extensible Sunnah Books Catalog modal demonstrating the modular book engine:
 * Al-Arba'oon Al-Nawawiyyah is active today, and future Sunnah collections drop right
 * into the same repository & station engine.
 */
@Composable
fun SunnahLibraryDialog(
    books: List<SunnahBook>,
    completedNawawiCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = StorybookColors.CreamBg,
            border = BorderStroke(3.dp, StorybookColors.TealMain),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RibbonBadge(
                        text = "مكتبة السنة النبوية للأطفال",
                        topColor = StorybookColors.TealMain,
                        bottomColor = StorybookColors.TealButtonBottom,
                        borderColor = StorybookColors.TealButtonBorder
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_library_dialog_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = StorybookColors.CoralDark)
                    }
                }

                books.filter { it.isAvailable }.forEach { book ->
                    ToyCard(
                        containerColor = if (StorybookColors.isDarkMode) StorybookColors.TealSoft else book.bgTint,
                        borderColor = book.accentColor,
                        lipColor = StorybookColors.TealButtonBorder,
                        cornerRadius = 22.dp,
                        lipHeight = 5.dp,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(book.accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = StorybookColors.StoryNavy
                                    )
                                }
                                Text(
                                    text = "${book.author} • ${ArabicUtils.toArabicDigits(book.hadithCount)} حديثاً",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = book.accentDark
                                )
                                Text(
                                    text = book.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StorybookColors.StoryMuted
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (book.isAvailable) StorybookColors.EmeraldSoft else StorybookColors.SurfaceWhite,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (book.isAvailable) StorybookColors.EmeraldMain else book.accentColor.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (book.isAvailable) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = StorybookColors.EmeraldDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = book.badgeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (book.isAvailable) StorybookColors.EmeraldDark else book.accentDark
                                    )
                                }
                            }

                            if (book.isAvailable) {
                                Text(
                                    text = "تقدّمك: ${ArabicUtils.toArabicDigits(completedNawawiCount)} / ${ArabicUtils.toArabicDigits(book.hadithCount)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = book.accentDark
                                )
                            }
                        }
                    }
                }

                ToyCard(
                    containerColor = StorybookColors.GoldSoft,
                    borderColor = StorybookColors.GoldMain.copy(alpha = 0.7f),
                    lipColor = StorybookColors.GoldDeep.copy(alpha = 0.45f),
                    cornerRadius = 22.dp,
                    lipHeight = 4.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                    modifier = Modifier.testTag("more_books_coming_soon_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = StorybookColors.GoldDeep,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = "المزيد قريباً إن شاء الله",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.StoryNavy,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

package com.nawawikids.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nawawikids.app.model.Hadith
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.RibbonBadge
import com.nawawikids.app.ui.components.ToyCard
import com.nawawikids.app.ui.theme.AmiriFontFamily
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils

@Composable
fun LearnTab(
    hadith: Hadith,
    onGoToMemorize: () -> Unit
) {
    val vocabPalettes = listOf(
        Triple(StorybookColors.CoralSoft, Color(0xFFFB7185), StorybookColors.CoralDeep),
        Triple(StorybookColors.GoldSoft, Color(0xFFFBBF24), StorybookColors.GoldDeep),
        Triple(StorybookColors.TealSoft, Color(0xFF2DD4BF), StorybookColors.TealDark),
        Triple(StorybookColors.SkySoft, Color(0xFF38BDF8), StorybookColors.SkyDark)
    )

    val lessonPalettes = listOf(
        Triple(StorybookColors.EmeraldSoft, StorybookColors.EmeraldBorder, StorybookColors.EmeraldDark),
        Triple(StorybookColors.GoldSoft, Color(0xFFFBBF24), StorybookColors.GoldDark),
        Triple(StorybookColors.CoralSoft, Color(0xFFFB7185), StorybookColors.CoralDark)
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Parchment Scroll Box for General Meaning
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StorybookColors.GoldDark, RoundedCornerShape(28.dp))
                .padding(bottom = 7.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFDE68A), Color(0xFFFCD34D), Color(0xFFFBBF24))
                    )
                )
                .border(3.dp, StorybookColors.GoldDark, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = StorybookColors.GoldDeep
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoStories,
                            contentDescription = null,
                            tint = StorybookColors.GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "الْمَعْنَى الْعَامُّ لِلْحَدِيثِ",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.GoldLight
                        )
                    }
                }

                Text(
                    text = hadith.meaning,
                    fontFamily = AmiriFontFamily,
                    fontSize = 23.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F2009),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2. Vocabulary Ribbons
        if (hadith.ghareeb.isNotEmpty()) {
            ToyCard {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RibbonBadge(text = "مَعَانِي الْكَلِمَاتِ")
                }
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    hadith.ghareeb.forEachIndexed { idx, item ->
                        val (bg, border, ink) = vocabPalettes[idx % vocabPalettes.size]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(border, RoundedCornerShape(18.dp))
                                .padding(bottom = 3.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(bg)
                                .border(2.dp, border, RoundedCornerShape(18.dp))
                                .padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${item.w} :",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = ink
                                )
                                Text(
                                    text = item.m,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ink.copy(alpha = 0.92f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Lessons Learned
        if (hadith.lessons.isNotEmpty()) {
            ToyCard {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RibbonBadge(
                        text = "مَاذَا نَتَعَلَّمُ مِنَ الْحَدِيثِ؟",
                        topColor = StorybookColors.TealMain,
                        bottomColor = StorybookColors.TealDark,
                        borderColor = Color(0xFF115E59)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    hadith.lessons.forEachIndexed { idx, lesson ->
                        val (bg, border, badgeColor) = lessonPalettes[idx % lessonPalettes.size]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(badgeColor, RoundedCornerShape(18.dp))
                                .padding(bottom = 3.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(bg)
                                .border(2.dp, border, RoundedCornerShape(18.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ArabicUtils.toArabicDigits(idx + 1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = lesson,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = badgeColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 4. Authentic Source Card
        if (hadith.source.isNotBlank()) {
            ToyCard(
                containerColor = Color(0xFFFFFBEB),
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    AssetImage(
                        assetPath = "img/book_guide.png",
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "من كتاب الأربعين النووية (طبعة دار المنهاج)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = StorybookColors.GoldDeep
                        )
                        Text(
                            text = hadith.source,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = StorybookColors.StoryMuted
                        )
                    }
                }
            }
        }

        // Next Step CTA
        CandyButton(
            onClick = onGoToMemorize,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("goto_memorize_button"),
            topColor = Color(0xFFA78BFA),
            bottomColor = StorybookColors.VioletMain,
            borderColor = StorybookColors.VioletDark,
            lipColor = StorybookColors.VioletDeep
        ) {
            Text(
                text = "الخطوة ٣: هيا نحفظ ونسمّع بالصوت",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

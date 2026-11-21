package com.nawawikids.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nawawikids.app.R
import com.nawawikids.app.model.AvatarChoice
import com.nawawikids.app.ui.components.AssetImage
import com.nawawikids.app.ui.components.CandyButton
import com.nawawikids.app.ui.components.StorybookBackground
import com.nawawikids.app.ui.theme.StorybookColors
import kotlinx.coroutines.launch

/**
 * First-launch only interactive onboarding tour introducing the app's features
 * and letting the student optionally personalize their Hero profile before starting.
 */
@Composable
fun OnboardingScreen(
    initialName: String,
    initialAge: Int?,
    initialAvatar: AvatarChoice,
    onFinish: (String, Int?, AvatarChoice) -> Unit,
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    var nameInput by remember { mutableStateOf(initialName) }
    var ageText by remember { mutableStateOf(initialAge?.toString() ?: "") }
    var avatarInput by remember { mutableStateOf(initialAvatar) }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    StorybookBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusTop, bottom = navBottom),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Step Label + Skip button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        border = BorderStroke(1.5.dp, StorybookColors.GoldBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = StorybookColors.GoldDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "جولة سريعة في التطبيق",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = StorybookColors.StoryNavy
                            )
                        }
                    }

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "تخطّي",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = StorybookColors.TealDark
                        )
                    }
                }

                // Main Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> WelcomeOnboardingSlide()
                        1 -> ListenAndUnderstandSlide()
                        2 -> VoiceTestAndGamesSlide()
                        else -> SetupHeroSlide(
                            nameInput = nameInput,
                            onNameChange = { nameInput = it.take(40) },
                            ageText = ageText,
                            onAgeChange = { raw ->
                                ageText = normalizeDigitsOnly(raw).take(3)
                            },
                            avatarInput = avatarInput,
                            onAvatarChange = { avatarInput = it }
                        )
                    }
                }

                // Bottom Navigation Dock (Dots + Back/Next or Start CTA)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { idx ->
                            val isSelected = pagerState.currentPage == idx
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 28.dp else 9.dp,
                                label = "dot_width"
                            )
                            val color by animateColorAsState(
                                targetValue = if (isSelected) StorybookColors.TealMain else StorybookColors.GoldBorder,
                                label = "dot_color"
                            )
                            Box(
                                modifier = Modifier
                                    .height(9.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        scope.launch { pagerState.animateScrollToPage(idx) }
                                    }
                            )
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagerState.currentPage > 0) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White,
                                border = BorderStroke(2.dp, StorybookColors.GoldBorder),
                                modifier = Modifier
                                    .height(52.dp)
                                    .clickable {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                    .testTag("onboarding_prev_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "السابق",
                                        tint = StorybookColors.StoryNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "السابق",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = StorybookColors.StoryNavy
                                    )
                                }
                            }
                        }

                        val isLastPage = pagerState.currentPage == 3
                        CandyButton(
                            onClick = {
                                if (!isLastPage) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                } else {
                                    val parsedAge = parseOpenAge(ageText)
                                    onFinish(nameInput, parsedAge, avatarInput)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag(if (isLastPage) "onboarding_start_button" else "onboarding_next_button"),
                            topColor = if (isLastPage) Color(0xFF34D399) else StorybookColors.GoldLight,
                            bottomColor = if (isLastPage) Color(0xFF059669) else StorybookColors.GoldMain,
                            borderColor = if (isLastPage) Color(0xFF047857) else StorybookColors.GoldDark,
                            lipColor = if (isLastPage) Color(0xFF064E3B) else StorybookColors.GoldDeep,
                            contentPadding = PaddingValues(vertical = 13.dp)
                        ) {
                            AnimatedContent(targetState = isLastPage, label = "cta_text") { last ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (last) {
                                        Icon(
                                            imageVector = Icons.Rounded.RocketLaunch,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "ابدأ رحلة الأربعين!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    } else {
                                        Text(
                                            text = "التالي",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = StorybookColors.StoryInk
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = StorybookColors.StoryInk,
                                            modifier = Modifier.size(20.dp)
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
}

@Composable
private fun WelcomeOnboardingSlide() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFF8FD8F8),
            border = BorderStroke(4.dp, Color.White),
            shadowElevation = 10.dp,
            modifier = Modifier.size(188.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_baraem_app_icon),
                contentDescription = "أيقونة رحلة الأربعين النووية",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "مرحباً بك في رحلة\nالأربعين النووية!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = StorybookColors.StoryNavy,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "مغامرة ممتعة وتفاعلية لحفظ وفهم ٤٢ حديثاً نبوياً شريفاً مع عمر ومريم، مقسمة في ٤ محطات مصورة تناسب جميع الأعمار.",
            style = MaterialTheme.typography.bodyLarge,
            color = StorybookColors.StoryMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(18.dp))

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            border = BorderStroke(2.dp, StorybookColors.TealMain.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssetImage(
                    assetPath = "img/book_guide.png",
                    contentDescription = null,
                    modifier = Modifier.size(54.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "خريطة تقدم ذكية ومحفوظة دائماً",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = StorybookColors.TealDark
                    )
                    Text(
                        text = "كل خطوة تنجزها تُحفظ تلقائياً مع نجومك ونقاطك ورتبتك لتكمل رحلتك في أي وقت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StorybookColors.StoryMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ListenAndUnderstandSlide() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(StorybookColors.TealDark, StorybookColors.TealMain)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AssetImage(
                    assetPath = "img/boy_guide.png",
                    contentDescription = "عمر",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "الخطوة الأولى والثانية",
                        style = MaterialTheme.typography.labelMedium,
                        color = StorybookColors.GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "استمع للحديث وافهم معناه بسهولة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.Headphones,
            iconBg = StorybookColors.TealSoft,
            iconTint = StorybookColors.TealDark,
            title = "استماع متزامن كلمة بكلمة",
            description = "استمع لتلاوة الحديث واضحة ومشكّلة مع تلوين كل كلمة لحظة نطقها، واضغط على أي كلمة لسماع نطقها الصحيح وتكرارها."
        )

        Spacer(Modifier.height(12.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.Lightbulb,
            iconBg = StorybookColors.GoldSoft,
            iconTint = StorybookColors.GoldDark,
            title = "شرح مبسّط ومعاني الكلمات",
            description = "تعرّف على معاني المفردات الصعبة، واقرأ شرحاً جميلاً للحديث مع دروس عملية نطبقها في حياتنا اليومية."
        )
    }
}

@Composable
private fun VoiceTestAndGamesSlide() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(StorybookColors.VioletDark, StorybookColors.CoralMain)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AssetImage(
                    assetPath = "img/girl_guide.png",
                    contentDescription = "مريم",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "الخطوة الثالثة والرابعة",
                        style = MaterialTheme.typography.labelMedium,
                        color = StorybookColors.GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "سمّع بصوتك والعب واجمع النجوم!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.Mic,
            iconBg = StorybookColors.VioletSoft,
            iconTint = StorybookColors.VioletDark,
            title = "اختبار الحفظ الفوري بصوتك",
            description = "اقرأ الحديث بصوتك لتظهر الكلمات واحدةً تلو الأخرى تلقائياً، وإذا أخطأت يكتشف التطبيق الخطأ فوراً ويساعدك على الإعادة."
        )

        Spacer(Modifier.height(10.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.Extension,
            iconBg = StorybookColors.CoralSoft,
            iconTint = StorybookColors.CoralDark,
            title = "ألعاب واختبارات ممتعة",
            description = "ثبّت حفظك عبر اختبار الأبطال، ولعبة ترتيب كلمات الحديث، ولعبة توصيل المعاني."
        )

        Spacer(Modifier.height(10.dp))

        OnboardingFeatureCard(
            icon = Icons.Rounded.EmojiEvents,
            iconBg = StorybookColors.GoldSoft,
            iconTint = StorybookColors.GoldDark,
            title = "شهادة تقدير باسمك",
            description = "كلما أنجزت الأحاديث ارتفعت رتبتك، وعند إتمام الرحلة تحصل على شهادة إنجاز عربية فاخرة باسمك قابلة للمشاركة."
        )
    }
}

@Composable
private fun SetupHeroSlide(
    nameInput: String,
    onNameChange: (String) -> Unit,
    ageText: String,
    onAgeChange: (String) -> Unit,
    avatarInput: AvatarChoice,
    onAvatarChange: (AvatarChoice) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "جهّز بطاقة البطل",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = StorybookColors.StoryNavy,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "اختر رفيقك المفضل واكتب اسمك وعمرك (يمكنك التعديل لاحقاً في أي وقت).",
            style = MaterialTheme.typography.bodyMedium,
            color = StorybookColors.StoryMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Companion picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvatarChoice.entries.forEach { choice ->
                val selected = avatarInput == choice
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (selected) StorybookColors.TealSoft else Color.White,
                    border = BorderStroke(
                        width = if (selected) 3.dp else 1.5.dp,
                        color = if (selected) StorybookColors.TealMain else StorybookColors.GoldBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAvatarChange(choice) }
                        .testTag("onboarding_avatar_${choice.key}")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AssetImage(
                                assetPath = choice.assetPath,
                                contentDescription = choice.displayName,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(StorybookColors.TealMain),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = choice.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selected) StorybookColors.TealDark else StorybookColors.StoryInk
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(2.dp, StorybookColors.GoldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    label = { Text("اسم البطل (يظهر في الرحلة والشهادة)") },
                    placeholder = { Text("مثال: يوسف أو سارة") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StorybookColors.TealMain,
                        unfocusedBorderColor = StorybookColors.GoldBorder,
                        focusedContainerColor = StorybookColors.CreamBg.copy(alpha = 0.4f),
                        unfocusedContainerColor = StorybookColors.CreamBg.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )

                OutlinedTextField(
                    value = ageText,
                    onValueChange = onAgeChange,
                    label = { Text("العمر بالسنوات (اختياري ومفتوح)") },
                    placeholder = { Text("اكتب عمرك هنا") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StorybookColors.TealMain,
                        unfocusedBorderColor = StorybookColors.GoldBorder,
                        focusedContainerColor = StorybookColors.CreamBg.copy(alpha = 0.4f),
                        unfocusedContainerColor = StorybookColors.CreamBg.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_age_input")
                )
            }
        }
    }
}

@Composable
private fun OnboardingFeatureCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, iconTint.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StorybookColors.StoryNavy
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = StorybookColors.StoryMuted
                )
            }
        }
    }
}

internal fun normalizeDigitsOnly(raw: String): String {
    val sb = StringBuilder()
    for (ch in raw) {
        when (ch) {
            in '0'..'9' -> sb.append(ch)
            in '٠'..'٩' -> sb.append(('0'.code + (ch.code - '٠'.code)).toChar())
            in '۰'..'۹' -> sb.append(('0'.code + (ch.code - '۰'.code)).toChar())
        }
    }
    return sb.toString()
}

internal fun parseOpenAge(raw: String): Int? {
    val clean = normalizeDigitsOnly(raw)
    return clean.toIntOrNull()?.takeIf { it in 1..120 }
}

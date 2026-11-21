package com.nawawikids.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nawawikids.app.ui.theme.StorybookColors
import com.nawawikids.app.util.ArabicUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Playful storybook background gradient with subtle twinkling golden stars & dots.
 */
@Composable
fun StorybookBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFFDFF3FF),
                        0.20f to Color(0xFFF2FAFF),
                        0.55f to StorybookColors.CreamBg,
                        1.0f to StorybookColors.CreamWarm
                    )
                )
            )
            .drawBehind {
                val step = 110.dp.toPx()
                val starColor = StorybookColors.GoldMain.copy(alpha = 0.09f)
                val dotTeal = StorybookColors.TealMain.copy(alpha = 0.09f)
                val dotCoral = StorybookColors.CoralMain.copy(alpha = 0.08f)
                var y = step * 0.4f
                var row = 0
                while (y < size.height) {
                    var x = if (row % 2 == 0) step * 0.3f else step * 0.75f
                    while (x < size.width) {
                        drawCircle(color = starColor, radius = 4.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = dotTeal, radius = 2.5.dp.toPx(), center = Offset(x + 36.dp.toPx(), y + 28.dp.toPx()))
                        drawCircle(color = dotCoral, radius = 2.2.dp.toPx(), center = Offset(x - 28.dp.toPx(), y + 40.dp.toPx()))
                        x += step
                    }
                    y += step
                    row++
                }
            },
        content = content
    )
}

/**
 * Tactile 3D storybook card with crisp border and solid bottom lip shadow.
 */
@Composable
fun ToyCard(
    modifier: Modifier = Modifier,
    containerColor: Color = StorybookColors.SurfaceWhite,
    borderColor: Color = StorybookColors.GoldBorder,
    lipColor: Color = StorybookColors.GoldLip,
    cornerRadius: Dp = 26.dp,
    lipHeight: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .background(color = lipColor, shape = shape)
            .padding(bottom = lipHeight)
    ) {
        Surface(
            modifier = Modifier,
            shape = shape,
            color = containerColor,
            border = BorderStroke(2.5.dp, borderColor),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

/**
 * Scalloped golden storybook parchment frame for displaying the Hadith Matn.
 */
@Composable
fun CloudMatnCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val outerShape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .background(color = Color(0xFFE2B857), shape = outerShape)
            .padding(bottom = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(outerShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White, Color(0xFFFFFDF6))
                    )
                )
                .border(3.dp, Color(0xFFEAB308), outerShape)
                .drawBehind {
                    val inset = 8.dp.toPx()
                    drawRoundRect(
                        color = StorybookColors.GoldMain.copy(alpha = 0.35f),
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2, size.height - inset * 2),
                        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    )
                }
                .padding(contentPadding)
        ) {
            Column(content = content)
        }
    }
}

/**
 * Tactile 3D candy button with minimum 48dp touch target, ripple feedback, and bottom lip depth.
 */
@Composable
fun CandyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    topColor: Color = StorybookColors.TealMain,
    bottomColor: Color = StorybookColors.TealDark,
    borderColor: Color = StorybookColors.TealDark,
    lipColor: Color = StorybookColors.TealDark,
    contentColor: Color = Color.White,
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val alpha = if (enabled) 1f else 0.45f
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .background(color = lipColor.copy(alpha = alpha), shape = shape)
            .padding(bottom = 4.dp)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    listOf(topColor.copy(alpha = alpha), bottomColor.copy(alpha = alpha))
                ),
                shape = shape
            )
            .border(2.dp, borderColor.copy(alpha = alpha), shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Poster-inspired coral or custom ribbon header badge.
 */
@Composable
fun RibbonBadge(
    text: String,
    modifier: Modifier = Modifier,
    topColor: Color = Color(0xFFFB7185),
    bottomColor: Color = Color(0xFFE11D48),
    borderColor: Color = StorybookColors.CoralDeep,
    textColor: Color = Color.White
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .background(borderColor, shape)
            .padding(bottom = 3.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)), shape)
            .border(2.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black
        )
    }
}

/**
 * Giant golden 5-point star medallion displaying the hadith number; pulses when Qari announces the hadith.
 */
@Composable
fun GoldenStarMedallion(
    number: Int,
    pulsing: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 74.dp
) {
    val infinite = rememberInfiniteTransition(label = "starPulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (pulsing) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val outerR = this.size.minDimension * 0.46f
            val innerR = outerR * 0.46f
            val path = Path()
            for (i in 0 until 10) {
                val angle = -PI / 2.0 + i * PI / 5.0
                val r = if (i % 2 == 0) outerR else innerR
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            if (pulsing) {
                drawCircle(
                    color = StorybookColors.GoldLight.copy(alpha = 0.55f),
                    radius = outerR * 1.12f,
                    center = Offset(cx, cy)
                )
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B)))
            )
            drawPath(
                path = path,
                color = StorybookColors.GoldDark,
                style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round)
            )
        }
        Text(
            text = ArabicUtils.toArabicDigits(number),
            color = Color(0xFF5C2C06),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

/**
 * Loads bundled asset images (posters, Omar/Maryam avatars, stage scenes) via Coil.
 */
@Composable
fun AssetImage(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cleanPath = assetPath.removePrefix("assets/")
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/$cleanPath")
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

package com.example.music

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun VinylRecordView(
    artworkUrl: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    // Continuous rotation angle that halts in place on pause
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastFrameTime = withFrameNanos { it }
        while (isPlaying) {
            withFrameNanos { frameTime ->
                val dt = (frameTime - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTime
                // Spin at ~100 degrees per second (smooth vintage 33 RPM feel)
                rotationAngle = (rotationAngle + dt * 100f) % 360f
            }
        }
    }

    // Tonearm swings onto vinyl when playing (24 degrees) and retreats to rest when paused (0 degrees)
    val tonearmAngle by animateFloatAsState(
        targetValue = if (isPlaying) 26f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tonearmPivotAngle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Turntable Base Platter Shadow
        Box(
            modifier = Modifier
                .fillMaxSize(0.94f)
                .shadow(elevation = 16.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color(0xFF0A0D12) else Color(0xFFE2E8F0))
        )

        // 1. Spinning Vinyl Disc
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .rotate(rotationAngle)
                .clip(CircleShape)
                .background(Color(0xFF111317)),
            contentAlignment = Alignment.Center
        ) {
            // Concentric vinyl microgrooves & anisotropic shine reflection
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.width / 2f

                // Concentric groove lines
                val grooveSteps = 14
                for (i in 4..grooveSteps) {
                    val r = maxRadius * (i.toFloat() / (grooveSteps + 1))
                    drawCircle(
                        color = Color(0x18FFFFFF),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // Dual anisotropic vinyl light sheen (cross light cones)
                val sheenGradient = Brush.sweepGradient(
                    0.0f to Color(0x20FFFFFF),
                    0.25f to Color(0x02FFFFFF),
                    0.50f to Color(0x20FFFFFF),
                    0.75f to Color(0x02FFFFFF),
                    1.0f to Color(0x20FFFFFF)
                )
                drawCircle(
                    brush = sheenGradient,
                    radius = maxRadius,
                    center = center
                )
            }

            // Center Album Artwork Label
            Box(
                modifier = Modifier
                    .fillMaxSize(0.42f)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFF1F242C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = artworkUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Central spindle hole
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1216))
                        .border(1.5.dp, Color(0xFF94A3B8), CircleShape)
                )
            }
        }

        // 2. Realistic Tonearm Overlay (Stationed Top-Right)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp, end = 6.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            TonearmView(
                angle = tonearmAngle,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.size(width = 130.dp, height = 210.dp)
            )
        }
    }
}

@Composable
private fun TonearmView(
    angle: Float,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .graphicsLayer {
                // Pivot around the base mount at top-right
                transformOrigin = TransformOrigin(0.80f, 0.12f)
                rotationZ = angle
            }
    ) {
        val pivotX = size.width * 0.80f
        val pivotY = size.height * 0.12f

        // 1. Pivot Base Mount
        drawCircle(
            color = if (isDarkTheme) Color(0xFF1E2631) else Color(0xFF94A3B8),
            radius = 16.dp.toPx(),
            center = Offset(pivotX, pivotY)
        )
        drawCircle(
            color = if (isDarkTheme) Color(0xFF475569) else Color(0xFFE2E8F0),
            radius = 11.dp.toPx(),
            center = Offset(pivotX, pivotY)
        )
        drawCircle(
            color = Color(0xFF7C4DFF), // Sonora accent dot on pivot
            radius = 4.dp.toPx(),
            center = Offset(pivotX, pivotY)
        )

        // 2. Tonearm Metal Shaft (Angled bend toward center vinyl)
        val shaftColor = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF475569)
        val jointX = size.width * 0.44f
        val jointY = size.height * 0.60f
        val needleX = size.width * 0.22f
        val needleY = size.height * 0.86f

        drawLine(
            color = shaftColor,
            start = Offset(pivotX, pivotY),
            end = Offset(jointX, jointY),
            strokeWidth = 4.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = shaftColor,
            start = Offset(jointX, jointY),
            end = Offset(needleX, needleY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 3. Headshell Cartridge / Stylus
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(needleX - 10.dp.toPx(), needleY - 6.dp.toPx()),
            size = Size(20.dp.toPx(), 28.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
        // Red stylus tip accent
        drawCircle(
            color = Color(0xFFEF4444),
            radius = 2.5.dp.toPx(),
            center = Offset(needleX, needleY + 18.dp.toPx())
        )
    }
}
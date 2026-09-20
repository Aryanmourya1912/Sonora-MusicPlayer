package com.example.music

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun DynamicAudioVisualizer(
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    visualizerHeight: Dp = 44.dp
) {
    val barHeights = remember(barCount) {
        List(barCount) { Animatable(0.12f) }
    }

    // Phase angle driving harmonic frequency waves
    val phaseAnim = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var tick = 0f
            while (isActive) {
                tick += 0.14f
                phaseAnim.snapTo(tick)

                for (i in 0 until barCount) {
                    val normalizedIndex = i.toFloat() / barCount

                    // Combine three sine frequencies for natural audio spectrum movement
                    val wave1 = sin((normalizedIndex * 3.5f * PI + tick).toFloat())
                    val wave2 = sin((normalizedIndex * 7.0f * PI - tick * 1.5f).toFloat())
                    val wave3 = sin((normalizedIndex * 1.2f * PI + tick * 0.8f).toFloat())

                    // Bell curve envelope: higher energy in bass/mid-range, tapering at extremes
                    val envelope = sin(normalizedIndex * PI.toFloat()).coerceIn(0.25f, 1.0f)
                    val rawHeight = ((wave1 * 0.45f + wave2 * 0.35f + wave3 * 0.20f) + 1f) / 2f
                    val jitter = Random.nextFloat() * 0.18f

                    val target = ((rawHeight * envelope) + jitter).coerceIn(0.12f, 0.98f)

                    barHeights[i].animateTo(
                        targetValue = target,
                        animationSpec = tween(
                            durationMillis = 85,
                            easing = LinearEasing
                        )
                    )
                }
            }
        } else {
            // Decay to flat resting state when paused
            for (i in 0 until barCount) {
                barHeights[i].animateTo(
                    targetValue = 0.08f,
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(visualizerHeight)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val totalBarWidth = canvasWidth / barCount
        val barWidth = totalBarWidth * 0.62f
        val gap = totalBarWidth * 0.38f

        val gradient = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.95f),
                secondaryColor.copy(alpha = 0.70f)
            ),
            startY = 0f,
            endY = canvasHeight
        )

        for (i in 0 until barCount) {
            val barH = (barHeights[i].value * canvasHeight).coerceAtLeast(3.dp.toPx())
            val x = i * (barWidth + gap) + (gap / 2f)
            val y = (canvasHeight - barH) / 2f // Vertically centered symmetrical sound wave

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
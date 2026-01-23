package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// ============================================================================
// FLOATING SCORE TEXT
// ============================================================================

/**
 * Floating score indicator that rises and fades out.
 * Shows "+1", "+5", etc. when bubbles are popped.
 */
@Composable
fun FloatingScoreText(
    score: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(900, easing = EaseOutCubic)
        ) { value, _ ->
            animationProgress = value
        }
        onComplete()
    }

    val yOffset = (-100.dp.value * animationProgress).dp
    val alpha = (1f - animationProgress * 0.8f).coerceIn(0f, 1f)
    val scale = 1f + animationProgress * 0.4f

    Text(
        text = "+$score",
        modifier = modifier
            .offset(y = yOffset)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            },
        color = color,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.4f),
                offset = Offset(2f, 2f),
                blurRadius = 6f
            )
        )
    )
}

/**
 * Manager for floating score texts.
 * Handles multiple simultaneous floating scores.
 */
@Composable
fun FloatingScoreManager(
    scores: List<FloatingScoreData>,
    onScoreComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        scores.forEach { scoreData ->
            key(scoreData.id) {
                Box(
                    modifier = Modifier
                        .offset(x = scoreData.x, y = scoreData.y)
                ) {
                    FloatingScoreText(
                        score = scoreData.score,
                        color = scoreData.color,
                        onComplete = { onScoreComplete(scoreData.id) }
                    )
                }
            }
        }
    }
}

data class FloatingScoreData(
    val id: String,
    val score: Int,
    val x: Dp,
    val y: Dp,
    val color: Color
)

// ============================================================================
// COMBO INDICATOR
// ============================================================================

/**
 * Animated combo indicator with pulsing effect.
 * Shows "COMBO x3!", "COMBO x5!", etc.
 */
@Composable
fun ComboIndicator(
    combo: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = combo >= 3,
        enter = scaleIn(
            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
        ) + fadeIn(animationSpec = tween(200)),
        exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "comboPulse")

        // Pulsing scale
        val pulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(250, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        // Color shift based on combo level
        val comboColor = when {
            combo >= 15 -> Color(0xFFDC2626) // Red for 15+
            combo >= 10 -> Color(0xFFF97316) // Orange for 10+
            combo >= 7 -> Color(0xFFEAB308)  // Yellow for 7+
            combo >= 5 -> Color(0xFF22C55E)  // Green for 5+
            else -> Color(0xFF3B82F6)        // Blue for 3+
        }

        // Glow intensity
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
        ) {
            Text(
                text = "COMBO",
                color = comboColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = comboColor.copy(alpha = glowAlpha),
                        blurRadius = 16f
                    )
                )
            )

            Text(
                text = "x$combo",
                color = comboColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(
                    shadow = Shadow(
                        color = comboColor.copy(alpha = glowAlpha),
                        blurRadius = 20f
                    )
                )
            )
        }
    }
}

/**
 * Big combo burst text that appears briefly for milestones
 */
@Composable
fun ComboBurstText(
    combo: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible && combo >= 5 && combo % 5 == 0,
        enter = scaleIn(initialScale = 0.5f, animationSpec = spring(dampingRatio = 0.4f)) + fadeIn(),
        exit = scaleOut(targetScale = 1.5f) + fadeOut(animationSpec = tween(400)),
        modifier = modifier
    ) {
        val burstColor = when {
            combo >= 20 -> Color(0xFFDC2626)
            combo >= 15 -> Color(0xFFF97316)
            combo >= 10 -> Color(0xFFEAB308)
            else -> Color(0xFF22C55E)
        }

        val burstText = when {
            combo >= 20 -> "🔥 UNSTOPPABLE! 🔥"
            combo >= 15 -> "⚡ AMAZING! ⚡"
            combo >= 10 -> "✨ AWESOME! ✨"
            combo >= 5 -> "👍 GREAT!"
            else -> ""
        }

        Text(
            text = burstText,
            color = burstColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(3f, 3f),
                    blurRadius = 8f
                )
            )
        )
    }
}

// ============================================================================
// ANIMATED BACKGROUND
// ============================================================================

/**
 * Animated gradient background with floating particles.
 * Creates an engaging, dynamic atmosphere for the game.
 */
@Composable
fun AnimatedGameBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 20
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    // Gradient color animation
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )

    // Generate gradient colors with subtle animation
    val topColor = lerp(
        Color(0xFFF5F5F4),     // stone-100
        Color(0xFFFAF5FF),     // purple tint
        colorShift * 0.3f
    )
    val middleColor = lerp(
        Color(0xFFE7E5E4),     // stone-200
        Color(0xFFFDF2F8),     // pink tint
        colorShift * 0.2f
    )
    val bottomColor = Color(0xFFFAFAF9) // stone-50

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(topColor, middleColor, bottomColor)
                )
            )
    ) {
        // Floating ambient particles
        FloatingParticlesLayer(particleCount = particleCount)
    }
}

/**
 * Layer of floating ambient particles
 */
@Composable
private fun FloatingParticlesLayer(particleCount: Int = 20) {
    val particles = remember {
        List(particleCount) {
            AmbientParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = 2f + Random.nextFloat() * 4f,
                speed = 0.00015f + Random.nextFloat() * 0.0003f,
                alpha = 0.08f + Random.nextFloat() * 0.15f,
                wobbleOffset = Random.nextFloat() * 1000f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing)
        ),
        label = "particleTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // Vertical movement (rising)
            val y = (particle.y - (time * particle.speed) % 1f + 1f) % 1f

            // Horizontal wobble
            val wobble = kotlin.math.sin((time + particle.wobbleOffset) * 0.01f) * 0.02f
            val x = (particle.x + wobble).coerceIn(0f, 1f)

            drawCircle(
                color = Color(0xFFD6D3D1).copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(x * size.width, y * size.height)
            )
        }
    }
}

private data class AmbientParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float,
    val wobbleOffset: Float
)

// ============================================================================
// SCREEN SHAKE EFFECT
// ============================================================================

/**
 * Wrapper that adds screen shake effect.
 * Use for big combos or special events.
 */
@Composable
fun ScreenShakeWrapper(
    shouldShake: Boolean,
    intensity: Float = 1f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    LaunchedEffect(shouldShake) {
        if (shouldShake) {
            repeat(6) {
                offsetX = (Random.nextFloat() - 0.5f) * 12f * intensity
                offsetY = (Random.nextFloat() - 0.5f) * 12f * intensity
                delay(40)
            }
            // Settle back
            offsetX = 0f
            offsetY = 0f
        }
    }

    Box(
        modifier = modifier.offset(x = offsetX.dp, y = offsetY.dp)
    ) {
        content()
    }
}

// ============================================================================
// ANIMATED SCORE COUNTER
// ============================================================================

/**
 * Score display that animates counting up.
 * Creates satisfying visual feedback when score increases.
 */
@Composable
fun AnimatedScoreCounter(
    score: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF292524),
    fontSize: Int = 48
) {
    var displayScore by remember { mutableStateOf(score) }
    var previousScore by remember { mutableStateOf(score) }
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(score) {
        if (score != previousScore && score > previousScore) {
            isAnimating = true
            val diff = score - previousScore
            val steps = minOf(diff, 15) // Max 15 steps for smooth animation
            val stepDelay = (200L / steps).coerceAtLeast(10L)

            repeat(steps) { i ->
                displayScore = previousScore + ((diff * (i + 1)) / steps)
                delay(stepDelay)
            }
            displayScore = score
            isAnimating = false
        } else {
            displayScore = score
        }
        previousScore = score
    }

    // Scale animation on change
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scoreScale"
    )

    Text(
        text = displayScore.toString(),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        color = textColor,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold
    )
}

// ============================================================================
// TIMER ANIMATIONS
// ============================================================================

/**
 * Animated timer that pulses when time is running low.
 */
@Composable
fun AnimatedTimer(
    timeRemaining: Int, // in seconds
    modifier: Modifier = Modifier,
    warningThreshold: Int = 10,
    criticalThreshold: Int = 5
) {
    val isWarning = timeRemaining <= warningThreshold
    val isCritical = timeRemaining <= criticalThreshold

    val infiniteTransition = rememberInfiniteTransition(label = "timer")

    // Pulse when warning
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCritical) 1.2f else if (isWarning) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isCritical) 300 else 500,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerPulse"
    )

    // Color animation
    val timerColor = when {
        isCritical -> Color(0xFFDC2626) // Red
        isWarning -> Color(0xFFF97316)  // Orange
        else -> Color(0xFF57534E)       // Stone-600
    }

    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeText = if (minutes > 0) {
        String.format("%d:%02d", minutes, seconds)
    } else {
        String.format("0:%02d", seconds)
    }

    Text(
        text = timeText,
        modifier = modifier.graphicsLayer {
            scaleX = pulse
            scaleY = pulse
        },
        color = timerColor,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        style = if (isCritical) {
            TextStyle(
                shadow = Shadow(
                    color = timerColor.copy(alpha = 0.5f),
                    blurRadius = 12f
                )
            )
        } else {
            TextStyle.Default
        }
    )
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Linear interpolation between two colors
 */
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}
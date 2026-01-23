package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// ============================================================================
// COLOR PALETTES - Carefully crafted for game feel
// ============================================================================

object GameColors {
    // Warm, energetic palette for positive feedback
    val ScoreGold = Color(0xFFFFD700)
    val ScoreOrange = Color(0xFFFF9500)
    val ScorePink = Color(0xFFFF6B9D)
    val ScorePurple = Color(0xFFB266FF)
    val ScoreCyan = Color(0xFF00E5FF)
    
    // Combo colors - vibrant gradient progression
    val ComboLevel1 = Color(0xFF7DD3FC) // Sky blue - starting combo
    val ComboLevel2 = Color(0xFF22D3EE) // Cyan - getting warmer
    val ComboLevel3 = Color(0xFFFBBF24) // Amber/Gold - nice streak
    val ComboLevel4 = Color(0xFFFB923C) // Orange - hot streak
    val ComboLevel5 = Color(0xFFF472B6) // Pink - amazing
    val ComboLevel6 = Color(0xFFC084FC) // Purple - legendary
    val ComboLevelMax = Color(0xFFFF6B6B) // Coral red - on fire
    
    // Glow colors (slightly desaturated for glow effect)
    val GlowGold = Color(0x80FFD700)
    val GlowPink = Color(0x80FF6B9D)
    val GlowCyan = Color(0x8000E5FF)
    val GlowPurple = Color(0x80B266FF)
    
    // Background accents
    val BackgroundWarm = Color(0xFFFFF7ED) // Warm cream
    val BackgroundCool = Color(0xFFF0F9FF) // Cool blue-white
}

// ============================================================================
// FLOATING SCORE TEXT - Enhanced with better animations
// ============================================================================

/**
 * Enhanced floating score with:
 * - Pop-in scale effect
 * - Smooth arc trajectory
 * - Gradient color
 * - Soft glow
 */
@Composable
fun FloatingScoreText(
    score: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var animationProgress by remember { mutableStateOf(0f) }
    
    // Initial pop scale
    var hasPopped by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Quick pop-in
        delay(16)
        hasPopped = true
        
        // Main float animation
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutCubic)
        ) { value, _ ->
            animationProgress = value
        }
        onComplete()
    }
    
    // Pop-in scale
    val popScale by animateFloatAsState(
        targetValue = if (hasPopped) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "popScale"
    )
    
    // Rise with slight arc
    val yOffset = (-120.dp.value * animationProgress).dp
    val xWobble = (kotlin.math.sin(animationProgress * Math.PI * 2) * 8).dp
    
    // Fade out in last 40%
    val alpha = when {
        animationProgress < 0.6f -> 1f
        else -> 1f - ((animationProgress - 0.6f) / 0.4f)
    }.coerceIn(0f, 1f)
    
    // Scale up slightly as it rises
    val floatScale = 1f + (animationProgress * 0.3f)
    
    // Choose display format
    val displayText = if (score >= 10) "+$score!" else "+$score"
    
    Box(
        modifier = modifier
            .offset(x = xWobble, y = yOffset)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = popScale * floatScale
                scaleY = popScale * floatScale
            },
        contentAlignment = Alignment.Center
    ) {
        // Glow layer (behind)
        Text(
            text = displayText,
            color = color.copy(alpha = 0.5f * alpha),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = 1.2f
                    scaleY = 1.2f
                }
                .blur(8.dp)
        )
        
        // Main text with gradient
        Text(
            text = displayText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        color
                    )
                ),
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.3f),
                    offset = Offset(2f, 3f),
                    blurRadius = 4f
                )
            )
        )
    }
}

/**
 * Manager for floating score texts with improved positioning
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
// COMBO INDICATOR - Completely redesigned
// ============================================================================

/**
 * Get combo color based on level - smooth gradient progression
 */
private fun getComboColor(combo: Int): Color {
    return when {
        combo >= 20 -> GameColors.ComboLevelMax
        combo >= 15 -> GameColors.ComboLevel6
        combo >= 12 -> GameColors.ComboLevel5
        combo >= 9 -> GameColors.ComboLevel4
        combo >= 6 -> GameColors.ComboLevel3
        combo >= 4 -> GameColors.ComboLevel2
        else -> GameColors.ComboLevel1
    }
}

/**
 * Get score color for floating text - matches bubble colors better
 */
fun getScoreColor(combo: Int): Color {
    return when {
        combo >= 10 -> GameColors.ScorePink
        combo >= 7 -> GameColors.ScoreOrange
        combo >= 5 -> GameColors.ScoreGold
        combo >= 3 -> GameColors.ScoreCyan
        else -> GameColors.ScoreGold
    }
}

/**
 * Enhanced combo indicator with:
 * - Smooth color transitions
 * - Pulsing glow effect
 * - Sleek pill design
 * - Animated counter
 */
@Composable
fun ComboIndicator(
    combo: Int,
    modifier: Modifier = Modifier
) {
    val isVisible = combo >= 3
    
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            initialScale = 0.5f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        ) + fadeIn(animationSpec = tween(150)),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        val comboColor by animateColorAsState(
            targetValue = getComboColor(combo),
            animationSpec = tween(300),
            label = "comboColor"
        )
        
        val infiniteTransition = rememberInfiniteTransition(label = "comboPulse")
        
        // Subtle pulse
        val pulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        
        // Glow intensity
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
        ) {
            // Glow background
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 1.3f
                        scaleY = 1.5f
                    }
                    .blur(16.dp)
                    .background(
                        comboColor.copy(alpha = glowAlpha * 0.6f),
                        RoundedCornerShape(24.dp)
                    )
            )
            
            // Main pill container
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                comboColor.copy(alpha = 0.95f),
                                comboColor.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Combo text
                    Text(
                        text = "COMBO",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Animated multiplier
                    AnimatedComboNumber(combo = combo)
                }
            }
        }
    }
}

/**
 * Animated combo number with bounce effect on change
 */
@Composable
private fun AnimatedComboNumber(combo: Int) {
    var displayCombo by remember { mutableStateOf(combo) }
    var isAnimating by remember { mutableStateOf(false) }
    
    LaunchedEffect(combo) {
        if (combo != displayCombo) {
            isAnimating = true
            delay(100)
            displayCombo = combo
            delay(200)
            isAnimating = false
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "numberScale"
    )
    
    Text(
        text = "×$displayCombo",
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        style = TextStyle(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.4f),
                offset = Offset(1f, 2f),
                blurRadius = 3f
            )
        )
    )
}

/**
 * Big combo milestone burst - shows on combo milestones (5, 10, 15, 20)
 */
@Composable
fun ComboBurstText(
    combo: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val shouldShow = isVisible && combo >= 5 && (combo == 5 || combo == 10 || combo == 15 || combo == 20 || combo == 25)
    
    AnimatedVisibility(
        visible = shouldShow,
        enter = scaleIn(
            initialScale = 0.3f,
            animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow)
        ) + fadeIn(animationSpec = tween(100)),
        exit = scaleOut(
            targetScale = 1.8f,
            animationSpec = tween(500, easing = EaseOutCubic)
        ) + fadeOut(animationSpec = tween(400)),
        modifier = modifier
    ) {
        val (burstText, burstColor, emoji) = when {
            combo >= 25 -> Triple("LEGENDARY", GameColors.ComboLevelMax, "👑")
            combo >= 20 -> Triple("UNSTOPPABLE", GameColors.ComboLevel6, "🔥")
            combo >= 15 -> Triple("ON FIRE", GameColors.ComboLevel5, "⚡")
            combo >= 10 -> Triple("AMAZING", GameColors.ComboLevel4, "✨")
            combo >= 5 -> Triple("NICE", GameColors.ComboLevel3, "👍")
            else -> Triple("", Color.White, "")
        }
        
        val infiniteTransition = rememberInfiniteTransition(label = "burst")
        
        val shimmer by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji with bounce
            Text(
                text = emoji,
                fontSize = 48.sp,
                modifier = Modifier.graphicsLayer {
                    rotationZ = kotlin.math.sin(shimmer * Math.PI.toFloat() * 2) * 10f
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Main burst text with gradient
            Box {
                // Glow
                Text(
                    text = burstText,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = burstColor.copy(alpha = 0.6f),
                    letterSpacing = 4.sp,
                    modifier = Modifier
                        .blur(12.dp)
                        .graphicsLayer {
                            scaleX = 1.1f
                            scaleY = 1.1f
                        }
                )
                
                // Text
                Text(
                    text = burstText,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                burstColor,
                                Color.White
                            ),
                            start = Offset(shimmer * 500f - 100f, 0f),
                            end = Offset(shimmer * 500f + 100f, 0f)
                        ),
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(3f, 4f),
                            blurRadius = 6f
                        )
                    )
                )
            }
        }
    }
}

// ============================================================================
// ANIMATED BACKGROUND - Enhanced with better colors
// ============================================================================

/**
 * Animated gradient background with floating particles
 */
@Composable
fun AnimatedGameBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 20
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )
    
    // Warmer, more inviting gradient
    val topColor = lerp(
        Color(0xFFFFFBF5),     // Warm white
        Color(0xFFFFF0F5),     // Lavender blush
        colorShift * 0.4f
    )
    val middleColor = lerp(
        Color(0xFFFFF7ED),     // Warm cream
        Color(0xFFFCE7F3),     // Pink tint
        colorShift * 0.3f
    )
    val bottomColor = lerp(
        Color(0xFFFAFAF9),     // Stone-50
        Color(0xFFF5F3FF),     // Purple tint
        colorShift * 0.2f
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(topColor, middleColor, bottomColor)
                )
            )
    ) {
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
                size = 2f + Random.nextFloat() * 5f,
                speed = 0.00012f + Random.nextFloat() * 0.00025f,
                alpha = 0.06f + Random.nextFloat() * 0.12f,
                wobbleOffset = Random.nextFloat() * 1000f,
                color = listOf(
                    Color(0xFFFFD6E0), // Light pink
                    Color(0xFFD4E4FF), // Light blue
                    Color(0xFFE8D4FF), // Light purple
                    Color(0xFFFFECD4)  // Light orange
                ).random()
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
            val y = (particle.y - (time * particle.speed) % 1f + 1f) % 1f
            val wobble = kotlin.math.sin((time + particle.wobbleOffset) * 0.008f) * 0.025f
            val x = (particle.x + wobble).coerceIn(0f, 1f)
            
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
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
    val wobbleOffset: Float,
    val color: Color = Color(0xFFD6D3D1)
)

// ============================================================================
// SCREEN SHAKE EFFECT
// ============================================================================

/**
 * Screen shake wrapper with improved feel
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
    var rotation by remember { mutableStateOf(0f) }
    
    LaunchedEffect(shouldShake) {
        if (shouldShake) {
            // Quick shake pattern
            repeat(5) { i ->
                val factor = 1f - (i * 0.15f) // Decay
                offsetX = (Random.nextFloat() - 0.5f) * 16f * intensity * factor
                offsetY = (Random.nextFloat() - 0.5f) * 10f * intensity * factor
                rotation = (Random.nextFloat() - 0.5f) * 2f * intensity * factor
                delay(35)
            }
            // Settle
            offsetX = 0f
            offsetY = 0f
            rotation = 0f
        }
    }
    
    Box(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        content()
    }
}

// ============================================================================
// ANIMATED SCORE COUNTER
// ============================================================================

/**
 * Score display that animates counting up
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
            val steps = minOf(diff, 15)
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
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.12f else 1f,
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
// ANIMATED TIMER
// ============================================================================

/**
 * Animated timer that pulses when time is running low
 */
@Composable
fun AnimatedTimer(
    timeRemaining: Int,
    modifier: Modifier = Modifier,
    warningThreshold: Int = 10,
    criticalThreshold: Int = 5
) {
    val isWarning = timeRemaining <= warningThreshold
    val isCritical = timeRemaining <= criticalThreshold
    
    val infiniteTransition = rememberInfiniteTransition(label = "timer")
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCritical) 1.15f else if (isWarning) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isCritical) 250 else 400,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerPulse"
    )
    
    val timerColor by animateColorAsState(
        targetValue = when {
            isCritical -> Color(0xFFEF4444)
            isWarning -> Color(0xFFF97316)
            else -> Color(0xFF57534E)
        },
        animationSpec = tween(300),
        label = "timerColor"
    )
    
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeText = if (minutes > 0) {
        String.format("%d:%02d", minutes, seconds)
    } else {
        String.format("0:%02d", seconds)
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Glow when critical
        if (isCritical) {
            Text(
                text = timeText,
                color = timerColor.copy(alpha = 0.4f),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .blur(8.dp)
                    .graphicsLayer {
                        scaleX = pulse * 1.2f
                        scaleY = pulse * 1.2f
                    }
            )
        }
        
        Text(
            text = timeText,
            modifier = Modifier.graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
            color = timerColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = if (isCritical) Shadow(
                    color = timerColor.copy(alpha = 0.5f),
                    blurRadius = 8f
                ) else null
            )
        )
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}
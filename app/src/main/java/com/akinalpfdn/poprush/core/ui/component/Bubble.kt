package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

/**
 * Enhanced Bubble composable with:
 * - Pop/burst animation
 * - Particle explosion effects
 * - Gradient rendering with 3D-like highlights
 * - Subtle idle wobble animation
 * - Dynamic shadows
 * - Sound effect support
 */
@Composable
fun Bubble(
    bubble: Bubble,
    shape: BubbleShape = BubbleShape.CIRCLE,
    bubbleSize: Dp = 48.dp,
    onClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    soundManager: PopSoundManager? = null
) {
    val context = LocalContext.current
    
    // Pop animation state
    var isPopping by remember { mutableStateOf(false) }
    var showParticles by remember { mutableStateOf(false) }
    var particleKey by remember { mutableStateOf(0) }
    
    // 1. Pop Scale Animation - expands then shrinks to nothing
    val popScale by animateFloatAsState(
        targetValue = when {
            isPopping -> 1.4f
            bubble.isPressed -> 0.85f
            else -> 1f
        },
        animationSpec = when {
            isPopping -> tween(120, easing = EaseOutBack)
            else -> spring(dampingRatio = 0.6f, stiffness = 400f)
        },
        label = "popScale"
    )
    
    // 2. Pop Alpha Animation - fade out during pop
    val popAlpha by animateFloatAsState(
        targetValue = if (isPopping) 0f else 1f,
        animationSpec = tween(150, easing = EaseInCubic),
        label = "popAlpha"
    )
    
    // 3. Press Translation (push down effect)
    val pressTranslation by animateFloatAsState(
        targetValue = if (bubble.isPressed && !isPopping) 3f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "pressY"
    )
    
    // 4. Idle Wobble Animation - makes bubbles feel alive
    val infiniteTransition = rememberInfiniteTransition(label = "bubbleIdle")
    
    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600 + (bubble.id % 400), // Slight variation per bubble
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )
    
    val wobbleScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000 + (bubble.id % 300),
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    
    // 5. Glow Animation for active state (classic mode)
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // 6. Speed Mode Transparency
    val transparencyAnimation by animateFloatAsState(
        targetValue = bubble.effectiveTransparency,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "transparency"
    )
    
    val bubbleColor = PastelColors.getColor(bubble.color)
    
    // Calculate final visual properties
    val finalAlpha = if (isPopping) popAlpha else transparencyAnimation
    val finalScale = popScale * (if (!isPopping && !bubble.isPressed) wobbleScale else 1f)
    
    // Dynamic shadow based on state
    val shadowElevation = when {
        isPopping -> 0.dp
        bubble.isPressed -> 2.dp
        else -> 8.dp
    }
    
    // Handle pop completion
    LaunchedEffect(isPopping) {
        if (isPopping) {
            delay(150) // Wait for pop animation
            showParticles = true
            particleKey++
            delay(400) // Wait for particles
            isPopping = false
            showParticles = false
        }
    }
    
    Box(
        modifier = modifier.size(bubbleSize),
        contentAlignment = Alignment.Center
    ) {
        // Particle explosion layer (behind or around the bubble)
        if (showParticles) {
            key(particleKey) {
                ParticleExplosion(
                    color = bubbleColor,
                    particleCount = 12,
                    modifier = Modifier.size(bubbleSize * 2)
                )
            }
        }
        
        // Main bubble
        Box(
            modifier = Modifier
                .size(bubbleSize)
                .graphicsLayer {
                    scaleX = finalScale
                    scaleY = finalScale
                    translationY = pressTranslation
                    rotationZ = if (!isPopping && !bubble.isPressed) wobbleRotation else 0f
                    alpha = finalAlpha
                }
                .shadow(
                    elevation = shadowElevation,
                    shape = getShapeOutline(shape),
                    ambientColor = bubbleColor.copy(alpha = 0.4f),
                    spotColor = bubbleColor.copy(alpha = 0.4f)
                )
                .clickable(
                    enabled = enabled && !isPopping,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        isPopping = true
                        soundManager?.playPop()
                        onClick(bubble.id)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Draw bubble with gradient and highlight
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBubbleWithGradient(
                    shape = shape,
                    baseColor = bubbleColor,
                    size = size.width,
                    isPressed = bubble.isPressed
                )
            }
            
            // Active state overlay (classic mode)
            if (bubble.isVisuallyActive && !bubble.isPressed && 
                transparencyAnimation > 0.1f && !bubble.isSpeedModeActive) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val fullSize = size.width
                    
                    if (bubble.isActive) {
                        // Dark overlay
                        drawCustomShape(
                            shape = shape,
                            color = Color.Black.copy(alpha = 0.15f * transparencyAnimation),
                            size = fullSize
                        )
                        
                        // Glowing core
                        val coreScale = 0.35f
                        val coreSize = fullSize * coreScale
                        val centerOffset = (fullSize - coreSize) / 2
                        
                        translate(left = centerOffset, top = centerOffset) {
                            drawCustomShape(
                                shape = shape,
                                color = Color(0xFF292524).copy(alpha = glowAlpha * transparencyAnimation),
                                size = coreSize
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Particle explosion effect when bubble pops
 */
@Composable
fun ParticleExplosion(
    color: Color,
    particleCount: Int = 12,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(particleCount) {
            PopParticle(
                angle = (360f / particleCount) * it + Random.nextFloat() * 20f - 10f,
                speed = 80f + Random.nextFloat() * 60f,
                size = 3f + Random.nextFloat() * 5f,
                rotationSpeed = Random.nextFloat() * 360f - 180f,
                color = if (Random.nextFloat() > 0.3f) color else color.copy(alpha = 0.7f)
                    .compositeOver(Color.White)
            )
        }
    }
    
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(400, easing = EaseOutQuad)
        ) { value, _ ->
            progress = value
        }
    }
    
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        particles.forEach { particle ->
            val distance = particle.speed * progress
            val angle = Math.toRadians(particle.angle.toDouble())
            val x = centerX + (cos(angle) * distance).toFloat()
            val y = centerY + (sin(angle) * distance).toFloat()
            
            // Fade out and shrink
            val alpha = (1f - progress).coerceIn(0f, 1f)
            val particleSize = particle.size * (1f - progress * 0.6f)
            
            // Add slight gravity effect
            val gravityOffset = progress * progress * 30f
            
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particleSize,
                center = Offset(x, y + gravityOffset)
            )
        }
    }
}

/**
 * Data class for particle properties
 */
private data class PopParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val rotationSpeed: Float,
    val color: Color
)

/**
 * Draw bubble with gradient for 3D effect
 */
private fun DrawScope.drawBubbleWithGradient(
    shape: BubbleShape,
    baseColor: Color,
    size: Float,
    isPressed: Boolean
) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2
    
    // Create gradient colors for 3D effect
    val lighterColor = baseColor.copy(alpha = 0.85f).compositeOver(Color.White)
    val darkerColor = baseColor.copy(alpha = 0.95f).compositeOver(Color.Black.copy(alpha = 0.15f))
    
    // Radial gradient from top-left (light source)
    val gradientBrush = Brush.radialGradient(
        colors = listOf(
            lighterColor,
            baseColor,
            if (isPressed) darkerColor else baseColor.copy(alpha = 0.9f).compositeOver(Color.Black.copy(alpha = 0.05f))
        ),
        center = Offset(size * 0.35f, size * 0.35f),
        radius = size * 0.85f
    )
    
    // Draw main shape with gradient
    when (shape) {
        BubbleShape.CIRCLE -> drawCircle(
            brush = gradientBrush,
            radius = radius,
            center = center
        )
        BubbleShape.SQUARE -> drawRoundRect(
            brush = gradientBrush,
            cornerRadius = CornerRadius(size * 0.2f, size * 0.2f),
            size = Size(size, size)
        )
        BubbleShape.STAR -> drawPath(createStarPath(center, radius), brush = gradientBrush)
        BubbleShape.HEXAGON -> drawPath(createHexagonPath(center, radius), brush = gradientBrush)
        BubbleShape.TRIANGLE -> drawPath(createTrianglePath(center, radius), brush = gradientBrush)
        BubbleShape.HEART -> drawPath(createHeartPath(center, size), brush = gradientBrush)
        else -> drawCircle(brush = gradientBrush, radius = radius, center = center)
    }
    
    // Add highlight spot (glass effect)
    val highlightRadius = size * 0.12f
    val highlightCenter = Offset(size * 0.32f, size * 0.32f)
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.6f),
                Color.White.copy(alpha = 0f)
            ),
            center = highlightCenter,
            radius = highlightRadius * 1.5f
        ),
        radius = highlightRadius,
        center = highlightCenter
    )
    
    // Add secondary smaller highlight
    val highlight2Center = Offset(size * 0.42f, size * 0.25f)
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = size * 0.04f,
        center = highlight2Center
    )
    
    // Add subtle rim light on bottom right
    if (!isPressed) {
        val rimCenter = Offset(size * 0.7f, size * 0.7f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0f)
                ),
                center = rimCenter,
                radius = size * 0.2f
            ),
            radius = size * 0.15f,
            center = rimCenter
        )
    }
}

// --- Shape Helpers ---

private fun getShapeOutline(shape: BubbleShape): Shape {
    return when (shape) {
        BubbleShape.CIRCLE -> CircleShape
        BubbleShape.SQUARE -> RoundedCornerShape(12.dp)
        else -> RoundedCornerShape(12.dp)
    }
}

private fun DrawScope.drawCustomShape(shape: BubbleShape, color: Color, size: Float) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2
    
    when (shape) {
        BubbleShape.CIRCLE -> drawCircle(color, radius, center)
        BubbleShape.SQUARE -> drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(size * 0.2f, size * 0.2f),
            size = Size(size, size)
        )
        BubbleShape.STAR -> drawPath(createStarPath(center, radius), color)
        BubbleShape.HEXAGON -> drawPath(createHexagonPath(center, radius), color)
        BubbleShape.TRIANGLE -> drawPath(createTrianglePath(center, radius), color)
        BubbleShape.HEART -> drawPath(createHeartPath(center, size), color)
        else -> drawCircle(color, radius, center)
    }
}

// --- Path Generators ---

private fun createStarPath(center: Offset, radius: Float): Path {
    val path = Path()
    val points = 5
    val innerRadius = radius / 2.5f
    var angle = -PI / 2
    
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += PI / points
    }
    path.close()
    return path
}

private fun createHexagonPath(center: Offset, radius: Float): Path {
    val path = Path()
    val points = 6
    var angle = -PI / 2
    
    for (i in 0 until points) {
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += (2 * PI) / points
    }
    path.close()
    return path
}

private fun createTrianglePath(center: Offset, radius: Float): Path {
    val path = Path()
    
    path.moveTo(center.x, center.y - radius)
    
    val angleRight = PI / 6
    path.lineTo(
        center.x + radius * cos(angleRight).toFloat(),
        center.y + radius * sin(angleRight).toFloat()
    )
    
    val angleLeft = 5 * PI / 6
    path.lineTo(
        center.x + radius * cos(angleLeft).toFloat(),
        center.y + radius * sin(angleLeft).toFloat()
    )
    
    path.close()
    return path
}

private fun createHeartPath(center: Offset, size: Float): Path {
    val path = Path()
    val width = size
    val height = size
    
    val bottomTipX = center.x
    val bottomTipY = center.y + (height * 0.35f)
    
    path.moveTo(bottomTipX, bottomTipY)
    
    path.cubicTo(
        center.x - width * 0.5f, center.y + height * 0.1f,
        center.x - width * 0.5f, center.y - height * 0.4f,
        center.x, center.y - height * 0.15f
    )
    
    path.cubicTo(
        center.x + width * 0.5f, center.y - height * 0.4f,
        center.x + width * 0.5f, center.y + height * 0.1f,
        bottomTipX, bottomTipY
    )
    
    path.close()
    return path
}
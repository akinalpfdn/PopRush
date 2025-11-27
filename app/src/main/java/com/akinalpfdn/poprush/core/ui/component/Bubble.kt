package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.sqrt

@Composable
fun Bubble(
    bubble: Bubble,
    shape: BubbleShape = BubbleShape.CIRCLE,
    bubbleSize: Dp = 48.dp, // Standard size matches React w-12
    onClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // 1. Physical Press Animation
    val pressScale by animateFloatAsState(
        targetValue = if (bubble.isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "press"
    )

    val pressTranslation by animateFloatAsState(
        targetValue = if (bubble.isPressed) 4f else 0f,
        label = "pressY"
    )

    // 2. Glow Animation (Pulse)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // 3. Speed Mode Transparency Animation
    val transparencyAnimation by animateFloatAsState(
        targetValue = bubble.effectiveTransparency,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "transparency"
    )

    val bubbleColor = PastelColors.getColor(bubble.color)

    // Apply transparency and darken slightly on press
    val finalColor = bubbleColor.copy(
        alpha = if (bubble.isPressed) transparencyAnimation * 0.9f else transparencyAnimation
    )

    Box(
        modifier = modifier
            .size(bubbleSize)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                translationY = pressTranslation
            }

            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(bubble.id) }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Draw Shape
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCustomShape(shape, finalColor, size.width)
        }

        // Active State (Classic Mode vs Speed Mode)
        if (bubble.isVisuallyActive && !bubble.isPressed && transparencyAnimation > 0.1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val fullSize = size.width

                when {
                    // Classic Mode: Traditional black overlay + glowing core
                    bubble.isActive -> {
                        // 1. Dark Overlay (Full Size)
                        drawCustomShape(
                            shape = shape,
                            color = Color.Black.copy(alpha = 0.2f * transparencyAnimation),
                            size = fullSize
                        )

                        // 2. Glowing Core (Inner Scaled Shape)
                        val coreScale = 0.4f
                        val coreSize = fullSize * coreScale
                        val centerOffset = (fullSize - coreSize) / 2

                        translate(left = centerOffset, top = centerOffset) {
                            drawCustomShape(
                                shape = shape,
                                color = Color(0xFF292524).copy(alpha = glowAlpha * transparencyAnimation), // Stone-800
                                size = coreSize
                            )
                        }
                    }

                    // Speed Mode: Enhanced glow effect for activated bubbles
                    bubble.isSpeedModeActive -> {
                        // 1. Bright glow effect for speed mode active bubbles
                        val glowScale = 0.8f
                        val glowSize = fullSize * glowScale
                        val glowCenterOffset = (fullSize - glowSize) / 2

                        translate(left = glowCenterOffset, top = glowCenterOffset) {
                            drawCustomShape(
                                shape = shape,
                                color = Color.White.copy(alpha = glowAlpha * 0.3f * transparencyAnimation),
                                size = glowSize
                            )
                        }

                        // 2. Inner bright core
                        val coreScale = 0.5f
                        val coreSize = fullSize * coreScale
                        val centerOffset = (fullSize - coreSize) / 2

                        translate(left = centerOffset, top = centerOffset) {
                            drawCustomShape(
                                shape = shape,
                                color = Color(0xFF292524).copy(alpha = glowAlpha * transparencyAnimation), // Stone-800
                                size = coreSize
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Shape Helpers ---

private fun getShapeOutline(shape: BubbleShape): Shape {
    return when (shape) {
        BubbleShape.CIRCLE -> CircleShape
        BubbleShape.SQUARE -> RoundedCornerShape(12.dp)
        else -> RoundedCornerShape(12.dp) // Fallback for shadows on complex shapes
    }
}

private fun DrawScope.drawCustomShape(shape: BubbleShape, color: Color, size: Float) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2

    when (shape) {
        BubbleShape.CIRCLE -> drawCircle(color, radius, center)
        BubbleShape.SQUARE -> drawRoundRect(
            color = color,
            // Changed to relative size for consistent look across different sizes
            cornerRadius = CornerRadius(size * 0.2f, size * 0.2f),
            size = Size(size, size)
        )
        // Fixed: Now calls a specific Star path function
        BubbleShape.STAR -> drawPath(createStarPath(center, radius), color)
        BubbleShape.HEXAGON -> drawPath(createHexagonPath(center, radius), color)
        BubbleShape.TRIANGLE -> drawPath(createTrianglePath(center, radius), color)
        BubbleShape.HEART -> drawPath(createHeartPath(center, size), color) // Heart needs full size for better aspect ratio
        else -> drawCircle(color, radius, center)
    }
}

// --- Path Generators ---

// Generates a standard 5-pointed star
private fun createStarPath(center: Offset, radius: Float): Path {
    val path = Path()
    val points = 5
    val innerRadius = radius / 2.5f // Ratio for a classic star look
    var angle = -PI / 2 // Start at top (90 degrees)

    // Calculate 10 points (5 outer, 5 inner)
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

// Generates a regular hexagon
private fun createHexagonPath(center: Offset, radius: Float): Path {
    val path = Path()
    val points = 6
    var angle = -PI / 2 // Start at top

    for (i in 0 until points) {
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += (2 * PI) / points
    }
    path.close()
    return path
}

// Generates an equilateral triangle
private fun createTrianglePath(center: Offset, radius: Float): Path {
    val path = Path()

    // Top point
    path.moveTo(center.x, center.y - radius)

    // Bottom Right (angles relative to center)
    val angleRight = PI / 6 // 30 degrees
    path.lineTo(
        center.x + radius * cos(angleRight).toFloat(),
        center.y + radius * sin(angleRight).toFloat()
    )

    // Bottom Left
    val angleLeft = 5 * PI / 6 // 150 degrees
    path.lineTo(
        center.x + radius * cos(angleLeft).toFloat(),
        center.y + radius * sin(angleLeft).toFloat()
    )

    path.close()
    return path
}

// Generates a heart shape using Cubic Bezier curves
private fun createHeartPath(center: Offset, size: Float): Path {
    val path = Path()
    val width = size
    val height = size

    // Starting point (Bottom tip)
    val bottomTipX = center.x
    val bottomTipY = center.y + (height * 0.35f)

    path.moveTo(bottomTipX, bottomTipY)

    // Left curve (Bottom to Top-Left)
    path.cubicTo(
        center.x - width * 0.5f, center.y + height * 0.1f, // Control point 1
        center.x - width * 0.5f, center.y - height * 0.4f, // Control point 2
        center.x, center.y - height * 0.15f                // Top middle dip
    )

    // Right curve (Top-Right to Bottom)
    path.cubicTo(
        center.x + width * 0.5f, center.y - height * 0.4f, // Control point 1
        center.x + width * 0.5f, center.y + height * 0.1f, // Control point 2
        bottomTipX, bottomTipY                             // Back to start
    )

    path.close()
    return path
}
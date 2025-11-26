package com.akinalpfdn.poprush.core.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akinalpfdn.poprush.core.domain.model.Bubble
import com.akinalpfdn.poprush.core.domain.model.BubbleColor
import com.akinalpfdn.poprush.core.domain.model.BubbleShape
import com.akinalpfdn.poprush.core.ui.theme.PastelColors
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Custom Composable for rendering game bubbles with different shapes and animations.
 *
 * @param bubble The bubble data to render
 * @param shape The visual shape to use for the bubble
 * @param isActive Whether the bubble is currently lit/active
 * @param isPressed Whether the bubble has been pressed
 * @param bubbleSize Size of the bubble
 * @param onClick Callback when bubble is clicked
 * @param modifier Additional modifier for the bubble
 * @param enabled Whether the bubble is interactive
 */
@Composable
fun Bubble(
    bubble: Bubble,
    shape: BubbleShape = BubbleShape.CIRCLE,
    isActive: Boolean = bubble.isActive,
    isPressed: Boolean = bubble.isPressed,
    bubbleSize: Dp = 48.dp,
    onClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Animation states
    val pressAnimation by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = EaseOutCubic
        ),
        label = "pressAnimation"
    )

    val glowAnimation by animateFloatAsState(
        targetValue = if (isActive && !isPressed) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAnimation"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isActive && !isPressed) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnimation"
    )

    // Color selection
    val bubbleColor = PastelColors.getColor(bubble.color)
    val pressedColor = PastelColors.getPressedColor(bubble.color)

    val currentColor = when {
        isPressed -> pressedColor
        isActive -> bubbleColor
        else -> bubbleColor.copy(alpha = 0.7f)
    }

    val density = LocalDensity.current

    when (shape) {
        BubbleShape.CIRCLE -> CircleBubble(
            bubble = bubble,
            color = currentColor,
            bubbleSize = bubbleSize,
            pressAnimation = pressAnimation,
            glowAnimation = glowAnimation,
            scaleAnimation = scaleAnimation,
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
        )

        BubbleShape.SQUARE -> SquareBubble(
            bubble = bubble,
            color = currentColor,
            bubbleSize = bubbleSize,
            pressAnimation = pressAnimation,
            glowAnimation = glowAnimation,
            scaleAnimation = scaleAnimation,
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
        )

        BubbleShape.STAR -> CustomShapeBubble(
            bubble = bubble,
            shape = BubbleShape.STAR,
            color = currentColor,
            bubbleSize = bubbleSize,
            pressAnimation = pressAnimation,
            glowAnimation = glowAnimation,
            scaleAnimation = scaleAnimation,
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
        )

        BubbleShape.TRIANGLE -> CustomShapeBubble(
            bubble = bubble,
            shape = BubbleShape.TRIANGLE,
            color = currentColor,
            bubbleSize = bubbleSize,
            pressAnimation = pressAnimation,
            glowAnimation = glowAnimation,
            scaleAnimation = scaleAnimation,
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
        )

        BubbleShape.HEART -> CustomShapeBubble(
            bubble = bubble,
            shape = BubbleShape.HEART,
            color = currentColor,
            bubbleSize = bubbleSize,
            pressAnimation = pressAnimation,
            glowAnimation = glowAnimation,
            scaleAnimation = scaleAnimation,
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
        )
    }
}

/**
 * Circle bubble implementation.
 */
@Composable
private fun CircleBubble(
    bubble: Bubble,
    color: Color,
    bubbleSize: Dp,
    pressAnimation: Float,
    glowAnimation: Float,
    scaleAnimation: Float,
    onClick: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(bubbleSize)
            .scale(scaleAnimation * pressAnimation, pivotX = 0.5f, pivotY = 0.5f)
            .shadow(
                elevation = if (bubble.isActive) 8.dp else 4.dp,
                shape = CircleShape,
                ambientColor = color.copy(alpha = 0.3f),
                spotColor = color.copy(alpha = 0.5f)
            )
            .background(color, CircleShape)
            .clickable(
                enabled = enabled && bubble.canBePressed,
                onClick = { onClick(bubble.id) },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner glow effect for active bubbles
        if (bubble.isActive && !bubble.isPressed) {
            Box(
                modifier = Modifier
                    .size(bubbleSize * 0.6f)
                    .background(
                        color = Color.White.copy(alpha = 0.6f * glowAnimation),
                        CircleShape
                    )
            )
        }

        // Press indicator
        if (bubble.isPressed) {
            Box(
                modifier = Modifier
                    .size(bubbleSize * 0.3f)
                    .background(
                        color = Color.Black.copy(alpha = 0.2f),
                        CircleShape
                    )
            )
        }
    }
}

/**
 * Square bubble implementation.
 */
@Composable
private fun SquareBubble(
    bubble: Bubble,
    color: Color,
    bubbleSize: Dp,
    pressAnimation: Float,
    glowAnimation: Float,
    scaleAnimation: Float,
    onClick: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(bubbleSize)
            .scale(scaleAnimation * pressAnimation, pivotX = 0.5f, pivotY = 0.5f)
            .shadow(
                elevation = if (bubble.isActive) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = color.copy(alpha = 0.3f),
                spotColor = color.copy(alpha = 0.5f)
            )
            .background(color, RoundedCornerShape(12.dp))
            .clickable(
                enabled = enabled && bubble.canBePressed,
                onClick = { onClick(bubble.id) },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner glow effect for active bubbles
        if (bubble.isActive && !bubble.isPressed) {
            Box(
                modifier = Modifier
                    .size(bubbleSize * 0.6f)
                    .background(
                        color = Color.White.copy(alpha = 0.6f * glowAnimation),
                        RoundedCornerShape(8.dp)
                    )
            )
        }

        // Press indicator
        if (bubble.isPressed) {
            Box(
                modifier = Modifier
                    .size(bubbleSize * 0.3f)
                    .background(
                        color = Color.Black.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * Custom shape bubble implementation (Hexagon, Triangle, Heart).
 */
@Composable
private fun CustomShapeBubble(
    bubble: Bubble,
    shape: BubbleShape,
    color: Color,
    bubbleSize: Dp,
    pressAnimation: Float,
    glowAnimation: Float,
    scaleAnimation: Float,
    onClick: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(bubbleSize)
            .scale(scaleAnimation * pressAnimation, pivotX = 0.5f, pivotY = 0.5f)
            .clickable(
                enabled = enabled && bubble.canBePressed,
                onClick = { onClick(bubble.id) },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val size = size.width
            val center = Offset(size / 2, size / 2)

            // Draw shadow
            if (bubble.isActive) {
                drawCustomShape(
                    shape = shape,
                    center = center,
                    size = size,
                    color = Color.Black.copy(alpha = 0.3f),
                    offset = Offset(4f, 4f)
                )
            }

            // Draw main bubble
            drawCustomShape(
                shape = shape,
                center = center,
                size = size,
                color = color
            )

            // Draw glow for active bubbles
            if (bubble.isActive && !bubble.isPressed) {
                drawCustomShape(
                    shape = shape,
                    center = center,
                    size = size * 0.6f,
                    color = Color.White.copy(alpha = 0.6f * glowAnimation)
                )
            }

            // Draw press indicator
            if (bubble.isPressed) {
                drawCustomShape(
                    shape = shape,
                    center = center,
                    size = size * 0.3f,
                    color = Color.Black.copy(alpha = 0.2f)
                )
            }
        }
    }
}

/**
 * Draws custom shapes on the canvas.
 */
private fun DrawScope.drawCustomShape(
    shape: BubbleShape,
    center: Offset,
    size: Float,
    color: Color,
    offset: Offset = Offset.Zero
) {
    val path = Path().apply {
        when (shape) {
            BubbleShape.STAR -> createStarPath(center + offset, size / 2)
            BubbleShape.TRIANGLE -> createTrianglePath(center + offset, size / 2)
            BubbleShape.HEART -> createHeartPath(center + offset, size / 2)
            else -> return
        }
    }

    drawPath(
        path = path,
        color = color
    )
}

/**
 * Creates a hexagon path.
 */
private fun Path.createHexagonPath(center: Offset, radius: Float) {
    val angleStep = 60f * (Math.PI / 180f).toFloat()

    moveTo(
        center.x + radius * kotlin.math.cos(0f).toFloat(),
        center.y + radius * kotlin.math.sin(0f).toFloat()
    )

    for (i in 1 until 6) {
        lineTo(
            center.x + radius * kotlin.math.cos(i * angleStep).toFloat(),
            center.y + radius * kotlin.math.sin(i * angleStep).toFloat()
        )
    }

    close()
}

/**
 * Creates a triangle path.
 */
private fun Path.createTrianglePath(center: Offset, radius: Float) {
    val height = radius * kotlin.math.sqrt(3f)

    moveTo(center.x, center.y - height / 2)
    lineTo(center.x - radius / 2, center.y + height / 2)
    lineTo(center.x + radius / 2, center.y + height / 2)
    close()
}

/**
 * Creates a star path.
 */
private fun Path.createStarPath(center: Offset, radius: Float) {
    val outerRadius = radius
    val innerRadius = radius * 0.4f
    val numPoints = 5

    for (i in 0 until numPoints * 2) {
        val angle = (i * PI / numPoints) - PI / 2
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + (cos(angle) * r).toFloat()
        val y = center.y + (sin(angle) * r).toFloat()

        if (i == 0) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }
    }

    close()
}

/**
 * Creates a heart path.
 */
private fun Path.createHeartPath(center: Offset, radius: Float) {
    val scaledRadius = radius * 0.8f

    // Create a simplified heart shape
    moveTo(center.x, center.y + scaledRadius * 0.3f)

    // Left curve
    cubicTo(
        center.x - scaledRadius * 0.5f, center.y - scaledRadius * 0.3f,
        center.x - scaledRadius * 0.5f, center.y - scaledRadius * 0.7f,
        center.x, center.y - scaledRadius * 0.4f
    )

    // Right curve
    cubicTo(
        center.x + scaledRadius * 0.5f, center.y - scaledRadius * 0.7f,
        center.x + scaledRadius * 0.5f, center.y - scaledRadius * 0.3f,
        center.x, center.y + scaledRadius * 0.3f
    )

    close()
}
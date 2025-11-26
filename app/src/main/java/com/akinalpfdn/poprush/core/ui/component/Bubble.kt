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

    val bubbleColor = PastelColors.getColor(bubble.color)
    // Darken slightly on press
    val finalColor = if (bubble.isPressed) bubbleColor.copy(alpha = 0.9f) else bubbleColor

    Box(
        modifier = modifier
            .size(bubbleSize)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                translationY = pressTranslation
            }
            .then(
                // Only show shadow if NOT pressed
                if (!bubble.isPressed) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = getShapeOutline(shape),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                } else Modifier
            )
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

        // Active State (Lit) - Black Overlay + Glow Core
        if (bubble.isActive && !bubble.isPressed) {
            // Dark Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCustomShape(shape, Color.Black.copy(alpha = 0.2f), size.width)
            }

            // Glowing Core
            Box(
                modifier = Modifier
                    .size(bubbleSize * 0.4f)
                    .background(
                        color = Color(0xFF292524).copy(alpha = glowAlpha), // Stone-800
                        shape = CircleShape
                    )
            )
        }
    }
}

// --- Shape Helpers ---

private fun getShapeOutline(shape: BubbleShape): Shape {
    return when (shape) {
        BubbleShape.CIRCLE -> CircleShape
        BubbleShape.SQUARE -> RoundedCornerShape(12.dp)
        else -> CircleShape // Fallback for shadows on complex shapes
    }
}

private fun DrawScope.drawCustomShape(shape: BubbleShape, color: Color, size: Float) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2

    when (shape) {
        BubbleShape.CIRCLE -> drawCircle(color, radius, center)
        BubbleShape.SQUARE -> drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            size = Size(size, size)
        )
        BubbleShape.HEXAGON -> drawPath(createHexagonPath(center, radius), color)
        BubbleShape.TRIANGLE -> drawPath(createTrianglePath(center, radius), color)
        BubbleShape.HEART -> drawPath(createHeartPath(center, radius), color)
        else -> drawCircle(color, radius, center)
    }
}

private fun createHexagonPath(center: Offset, radius: Float): Path {
    val path = Path()
    val angleStep = 60f * (PI / 180f).toFloat()
    for (i in 0 until 6) {
        val x = center.x + radius * cos(i * angleStep)
        val y = center.y + radius * sin(i * angleStep)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun createTrianglePath(center: Offset, radius: Float): Path {
    val path = Path()
    val height = radius * sqrt(3f)
    path.moveTo(center.x, center.y - height / 2)
    path.lineTo(center.x - radius, center.y + height / 2)
    path.lineTo(center.x + radius, center.y + height / 2)
    path.close()
    return path
}

private fun createHeartPath(center: Offset, radius: Float): Path {
    val path = Path()
    val width = radius * 2
    val height = radius * 2
    val topCurveHeight = height * 0.35f
    path.moveTo(center.x, center.y - height * 0.2f)
    path.cubicTo(
        center.x, center.y - height * 0.5f,
        center.x - width * 0.5f, center.y - height * 0.5f,
        center.x - width * 0.5f, center.y - height * 0.2f
    )
    path.cubicTo(
        center.x - width * 0.5f, center.y + height * 0.1f,
        center.x, center.y + height * 0.4f,
        center.x, center.y + height * 0.5f
    )
    path.cubicTo(
        center.x, center.y + height * 0.4f,
        center.x + width * 0.5f, center.y + height * 0.1f,
        center.x + width * 0.5f, center.y - height * 0.2f
    )
    path.cubicTo(
        center.x + width * 0.5f, center.y - height * 0.5f,
        center.x, center.y - height * 0.5f,
        center.x, center.y - height * 0.2f
    )
    path.close()
    return path
}
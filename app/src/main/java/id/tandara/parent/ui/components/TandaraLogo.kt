package id.tandara.parent.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tandara official book logo:
 * An open book with blue and cyan pages, and a green growth sprout in the center.
 */
@Composable
fun TandaraLogo(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    frameColor: Color = Color(0xFF2563EB),
    checkColor: Color = Color(0xFF06B6D4)
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Colors matching the official Tandara branding
            val leftPageColor = Color(0xFF2563EB) // Royal Blue
            val leftPageDark = Color(0xFF1D4ED8)
            val rightPageColor = Color(0xFF06B6D4) // Cyan / Aqua
            val rightPageLight = Color(0xFF22D3EE)
            val sproutColor = Color(0xFF10B981) // Emerald / Green

            // 1. Left Book Page
            val leftPagePath = Path().apply {
                moveTo(w * 0.48f, h * 0.32f)
                cubicTo(w * 0.35f, h * 0.22f, w * 0.20f, h * 0.24f, w * 0.10f, h * 0.32f)
                lineTo(w * 0.10f, h * 0.72f)
                cubicTo(w * 0.22f, h * 0.64f, w * 0.35f, h * 0.65f, w * 0.48f, h * 0.78f)
                close()
            }
            drawPath(
                path = leftPagePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(leftPageDark, leftPageColor),
                    startX = w * 0.10f,
                    endX = w * 0.48f
                )
            )

            // 2. Right Book Page
            val rightPagePath = Path().apply {
                moveTo(w * 0.52f, h * 0.32f)
                cubicTo(w * 0.65f, h * 0.22f, w * 0.80f, h * 0.24f, w * 0.90f, h * 0.32f)
                lineTo(w * 0.90f, h * 0.72f)
                cubicTo(w * 0.78f, h * 0.64f, w * 0.65f, h * 0.65f, w * 0.52f, h * 0.78f)
                close()
            }
            drawPath(
                path = rightPagePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(rightPageColor, rightPageLight),
                    startX = w * 0.52f,
                    endX = w * 0.90f
                )
            )

            // 3. Center Growth Sprout (Stem & Leaves)
            val stemPath = Path().apply {
                moveTo(w * 0.50f, h * 0.72f)
                lineTo(w * 0.50f, h * 0.24f)
            }
            drawPath(
                path = stemPath,
                color = sproutColor,
                style = Stroke(width = w * 0.05f, cap = StrokeCap.Round)
            )

            // Left sprout leaf
            val leftLeaf = Path().apply {
                moveTo(w * 0.50f, h * 0.38f)
                cubicTo(w * 0.42f, h * 0.32f, w * 0.40f, h * 0.20f, w * 0.48f, h * 0.18f)
                cubicTo(w * 0.50f, h * 0.24f, w * 0.50f, h * 0.32f, w * 0.50f, h * 0.38f)
                close()
            }
            drawPath(path = leftLeaf, color = sproutColor, style = Fill)

            // Right sprout leaf
            val rightLeaf = Path().apply {
                moveTo(w * 0.50f, h * 0.38f)
                cubicTo(w * 0.58f, h * 0.32f, w * 0.60f, h * 0.20f, w * 0.52f, h * 0.18f)
                cubicTo(w * 0.50f, h * 0.24f, w * 0.50f, h * 0.32f, w * 0.50f, h * 0.38f)
                close()
            }
            drawPath(path = rightLeaf, color = sproutColor, style = Fill)
        }
    }
}

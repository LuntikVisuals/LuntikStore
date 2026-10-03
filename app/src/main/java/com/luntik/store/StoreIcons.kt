package com.luntik.store

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Иконка корзины (тележка). */
@Composable
fun IconCart(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // handle
        drawLine(color, Offset(w * 0.12f, h * 0.22f), Offset(w * 0.28f, h * 0.22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.28f, h * 0.22f), Offset(w * 0.34f, h * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        // basket body
        val path = Path().apply {
            moveTo(w * 0.30f, h * 0.38f)
            lineTo(w * 0.88f, h * 0.38f)
            lineTo(w * 0.82f, h * 0.72f)
            lineTo(w * 0.36f, h * 0.72f)
            close()
        }
        drawPath(path, color, style = stroke)
        // wheels
        drawCircle(color, radius = w * 0.06f, center = Offset(w * 0.42f, h * 0.84f), style = stroke)
        drawCircle(color, radius = w * 0.06f, center = Offset(w * 0.72f, h * 0.84f), style = stroke)
    }
}

/** Круг со стрелкой вверх (загрузки / установщик). */
@Composable
fun IconDownloadUp(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // circle
        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w / 2f, h / 2f),
            style = stroke
        )
        // arrow up
        val cx = w / 2f
        drawLine(color, Offset(cx, h * 0.68f), Offset(cx, h * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(cx, h * 0.32f), Offset(w * 0.34f, h * 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(cx, h * 0.32f), Offset(w * 0.66f, h * 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

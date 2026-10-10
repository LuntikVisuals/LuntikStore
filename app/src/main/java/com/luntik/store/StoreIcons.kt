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

@Composable fun IconCart(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(color, Offset(w * 0.12f, h * 0.22f), Offset(w * 0.28f, h * 0.22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.28f, h * 0.22f), Offset(w * 0.34f, h * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        val path = Path().apply { moveTo(w * 0.30f, h * 0.38f); lineTo(w * 0.88f, h * 0.38f); lineTo(w * 0.82f, h * 0.72f); lineTo(w * 0.36f, h * 0.72f); close() }
        drawPath(path, color, style = stroke)
        drawCircle(color, radius = w * 0.06f, center = Offset(w * 0.42f, h * 0.84f), style = stroke)
        drawCircle(color, radius = w * 0.06f, center = Offset(w * 0.72f, h * 0.84f), style = stroke)
    }
}
@Composable fun IconDownloadUp(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(color, radius = w * 0.42f, center = Offset(w / 2f, h / 2f), style = stroke)
        val cx = w / 2f
        drawLine(color, Offset(cx, h * 0.68f), Offset(cx, h * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(cx, h * 0.32f), Offset(w * 0.34f, h * 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(cx, h * 0.32f), Offset(w * 0.66f, h * 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
@Composable fun IconGrid(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val g = w * 0.08f; val s = (w - g * 3) / 2f
        listOf(g to g, g * 2 + s to g, g to g * 2 + s, g * 2 + s to g * 2 + s).forEach { (x, y) -> drawRoundRect(color, Offset(x, y), Size(s, s), androidx.compose.ui.geometry.CornerRadius(w * 0.08f)) }
    }
}
@Composable fun IconBooks(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val h = size.toPx()
        drawRoundRect(color, Offset(w * 0.14f, h * 0.22f), Size(w * 0.18f, h * 0.58f), androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        drawRoundRect(color, Offset(w * 0.40f, h * 0.16f), Size(w * 0.18f, h * 0.64f), androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
        drawRoundRect(color, Offset(w * 0.66f, h * 0.26f), Size(w * 0.18f, h * 0.54f), androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
    }
}
@Composable fun IconGear(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val c = Offset(w / 2f, w / 2f)
        drawCircle(color, w * 0.18f, c)
        drawCircle(color, w * 0.34f, c, style = Stroke(w * 0.08f))
        repeat(6) { i ->
            val a = Math.toRadians((i * 60).toDouble())
            drawLine(color, Offset(c.x + kotlin.math.cos(a).toFloat() * w * 0.28f, c.y + kotlin.math.sin(a).toFloat() * w * 0.28f), Offset(c.x + kotlin.math.cos(a).toFloat() * w * 0.46f, c.y + kotlin.math.sin(a).toFloat() * w * 0.46f), strokeWidth = w * 0.09f, cap = StrokeCap.Round)
        }
    }
}
@Composable fun IconPerson(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val w = size.toPx()
        drawCircle(color, w * 0.16f, Offset(w / 2f, w * 0.32f))
        drawArc(color, 0f, 180f, false, Offset(w * 0.18f, w * 0.48f), Size(w * 0.64f, w * 0.5f))
    }
}

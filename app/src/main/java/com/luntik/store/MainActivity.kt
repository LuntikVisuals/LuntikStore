package com.luntik.store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent { StoreApp() }
    }
}

/* ---------- palette ---------- */
private object G {
    val Bg0 = Color(0xFF08080C)
    val Bg1 = Color(0xFF101018)
    val Bg2 = Color(0xFF16161F)
    val Glass = Color.White.copy(alpha = 0.07f)
    val GlassStrong = Color.White.copy(alpha = 0.11f)
    val Border = Color.White.copy(alpha = 0.13f)
    val BorderBright = Color.White.copy(alpha = 0.22f)
    val Text = Color(0xFFF2F2F7)
    val TextDim = Color.White.copy(alpha = 0.55f)
    val TextMute = Color.White.copy(alpha = 0.32f)
    val Accent = Color(0xFF8B9CFF)
    val Accent2 = Color(0xFFB8C0FF)
    val Green = Color(0xFF5CFFB0)
}

data class StoreAppItem(
    val id: String,
    val name: String,
    val tagline: String,
    val version: String,
    val accent: Color
)

@Composable
fun StoreApp() {
    val apps = remember {
        listOf(
            StoreAppItem("terminal", "LuntikTerminal", "Скрытый установщик", "0.3.0", G.Accent),
            StoreAppItem("ai", "LuntikAi", "Локальный ИИ-агент", "0.5.0", Color(0xFF7CFFB2)),
            StoreAppItem("lumina", "Lumina", "Liquid glass виджеты", "0.2.0", Color(0xFFFFB8E0))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(G.Bg0, G.Bg1, G.Bg0)))
    ) {
        // soft ambient orbs
        Canvas(Modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(G.Accent.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.12f),
                    radius = size.minDimension * 0.45f
                ),
                radius = size.minDimension * 0.45f,
                center = Offset(size.width * 0.15f, size.height * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF7CFFB2).copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.75f),
                    radius = size.minDimension * 0.4f
                ),
                radius = size.minDimension * 0.4f,
                center = Offset(size.width * 0.9f, size.height * 0.75f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Spacer(Modifier.height(28.dp))
                Header()
                Spacer(Modifier.height(28.dp))
                SectionTitle("Установщик")
                Spacer(Modifier.height(12.dp))
                TerminalCard()
                Spacer(Modifier.height(28.dp))
                SectionTitle("Приложения")
                Spacer(Modifier.height(12.dp))
            }

            items(apps.filter { it.id != "terminal" }) { app ->
                AppCard(app)
                Spacer(Modifier.height(12.dp))
            }

            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "v0.1.0  ·  LuntikVisuals",
                    color = G.TextMute,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = "LuntikStore",
            color = G.Text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "магазин приложений Luntik",
            color = G.TextDim,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = G.TextMute,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp
    )
}

@Composable
private fun TerminalCard() {
    val context = LocalContext.current
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(1.dp, G.BorderBright, shape)
            .padding(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(23.dp))
                .background(G.Bg2.copy(alpha = 0.85f))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // icon drawn with code
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(G.Accent.copy(alpha = 0.18f))
                        .border(1.dp, G.Accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(26.dp)) {
                        val s = size.minDimension
                        // terminal window frame
                        drawRoundRect(
                            color = G.Accent,
                            topLeft = Offset(s * 0.1f, s * 0.18f),
                            size = androidx.compose.ui.geometry.Size(s * 0.8f, s * 0.64f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                            style = Stroke(width = 2f)
                        )
                        drawLine(
                            color = G.Accent,
                            start = Offset(s * 0.1f, s * 0.38f),
                            end = Offset(s * 0.9f, s * 0.38f),
                            strokeWidth = 1.5f
                        )
                        // prompt cursor
                        drawLine(
                            color = G.Accent,
                            start = Offset(s * 0.22f, s * 0.55f),
                            end = Offset(s * 0.22f, s * 0.7f),
                            strokeWidth = 2.2f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("LuntikTerminal", color = G.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("скрытый установщик", color = G.TextDim, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Через Terminal скачивается этот магазин. После установки ярлык Terminal скрывается — открыть его можно только здесь.",
                color = G.TextDim,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(G.Accent)
                    .clickable {
                        try {
                            context.packageManager
                                .getLaunchIntentForPackage("com.luntik.terminal")
                                ?.let { context.startActivity(it) }
                        } catch (_: Exception) { }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Открыть Terminal",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AppCard(app: StoreAppItem) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(1.dp, G.Border, shape)
            .padding(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .background(G.Bg2.copy(alpha = 0.8f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // app icon — pure geometry
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(app.accent.copy(alpha = 0.15f))
                    .border(1.dp, app.accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(28.dp)) {
                    val s = size.minDimension
                    when (app.id) {
                        "ai" -> {
                            // node network
                            val nodes = listOf(
                                Offset(s * 0.5f, s * 0.2f),
                                Offset(s * 0.2f, s * 0.7f),
                                Offset(s * 0.8f, s * 0.7f),
                                Offset(s * 0.5f, s * 0.5f)
                            )
                            for (i in 0 until nodes.lastIndex) {
                                drawLine(app.accent, nodes[i], nodes.last(), strokeWidth = 1.5f)
                            }
                            nodes.forEach {
                                drawCircle(app.accent, radius = 3.5f, center = it)
                            }
                        }
                        else -> {
                            // layered glass rectangles for Lumina
                            drawRoundRect(
                                color = app.accent.copy(alpha = 0.5f),
                                topLeft = Offset(s * 0.15f, s * 0.25f),
                                size = androidx.compose.ui.geometry.Size(s * 0.7f, s * 0.5f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
                                style = Stroke(1.8f)
                            )
                            drawRoundRect(
                                color = app.accent,
                                topLeft = Offset(s * 0.28f, s * 0.38f),
                                size = androidx.compose.ui.geometry.Size(s * 0.44f, s * 0.28f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    color = G.Text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.tagline,
                    color = G.TextDim,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "v${app.version}",
                    color = G.TextMute,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(G.GlassStrong)
                    .border(1.dp, G.Border, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text("скоро", color = G.TextDim, fontSize = 12.sp)
            }
        }
    }
}

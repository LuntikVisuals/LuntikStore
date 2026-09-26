package com.luntik.store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

private object G {
    val Bg0 = Color(0xFF08080C)
    val Bg1 = Color(0xFF101018)
    val Bg2 = Color(0xFF16161F)
    val GlassStrong = Color.White.copy(alpha = 0.11f)
    val Border = Color.White.copy(alpha = 0.13f)
    val BorderBright = Color.White.copy(alpha = 0.22f)
    val Text = Color(0xFFF2F2F7)
    val TextDim = Color.White.copy(alpha = 0.55f)
    val TextMute = Color.White.copy(alpha = 0.32f)
    val Accent = Color(0xFF8B9CFF)
    val Green = Color(0xFF5CFFB0)
    val Pink = Color(0xFFFFB8E0)
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
            StoreAppItem("ai", "LuntikAi", "Локальный ИИ-агент", "0.5.0", G.Green),
            StoreAppItem("lumina", "Lumina", "Liquid glass виджеты", "0.2.0", G.Pink)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(G.Bg0, G.Bg1, G.Bg0)))
    ) {
        // ambient glow orbs (no Canvas — pure Box gradients)
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(G.Accent.copy(alpha = 0.16f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(G.Green.copy(alpha = 0.10f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Spacer(Modifier.height(28.dp))
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
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "УСТАНОВЩИК",
                    color = G.TextMute,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp
                )
                Spacer(Modifier.height(12.dp))
                TerminalCard()
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "ПРИЛОЖЕНИЯ",
                    color = G.TextMute,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp
                )
                Spacer(Modifier.height(12.dp))
            }

            items(apps) { app ->
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
                    textAlign = TextAlign.Center
                )
            }
        }
    }
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
                .background(G.Bg2.copy(alpha = 0.88f))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // terminal icon — geometry via nested boxes
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(G.Accent.copy(alpha = 0.18f))
                        .border(1.dp, G.Accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(G.Accent.copy(alpha = 0.5f), RoundedCornerShape(1.dp))
                        )
                        Box(
                            Modifier
                                .width(18.dp)
                                .height(3.dp)
                                .background(G.Accent, RoundedCornerShape(1.dp))
                        )
                        Box(
                            Modifier
                                .width(12.dp)
                                .height(3.dp)
                                .background(G.Accent.copy(alpha = 0.7f), RoundedCornerShape(1.dp))
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LuntikTerminal",
                        color = G.Text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "скрытый установщик",
                        color = G.TextDim,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

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
                        } catch (_: Exception) {
                        }
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
                .background(G.Bg2.copy(alpha = 0.85f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(app.accent.copy(alpha = 0.15f))
                    .border(1.dp, app.accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // simple geometric mark
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(2.dp, app.accent, RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(app.accent)
                )
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

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(G.GlassStrong)
                    .border(1.dp, G.Border, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(text = "скоро", color = G.TextDim, fontSize = 12.sp)
            }
        }
    }
}

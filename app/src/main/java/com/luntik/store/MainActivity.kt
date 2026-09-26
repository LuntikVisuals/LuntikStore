package com.luntik.store

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF8B9CFF),
                    background = Color(0xFF0A0A0F),
                    surface = Color(0xFF14141C),
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                StoreScreen()
            }
        }
    }
}

private object G {
    val Border = Color.White.copy(alpha = 0.14f)
    val Fill = Color.White.copy(alpha = 0.07f)
    val FillStrong = Color.White.copy(alpha = 0.11f)
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.62f)
    val TextMuted = Color.White.copy(alpha = 0.38f)
    val Accent = Color(0xFF8B9CFF)
    val BgDeep = Color(0xFF0A0A0F)
    val BgMid = Color(0xFF12121A)
}

@Composable
fun StoreScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(G.BgDeep, G.BgMid, G.BgDeep)
                )
            )
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "LuntikStore",
            color = G.TextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "центральный магазин приложений",
            color = G.TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Terminal card
        GlassCard(
            title = "LuntikTerminal",
            subtitle = "Скрытый установщик. Открыть терминал.",
            actionLabel = "Открыть Terminal",
            onAction = {
                try {
                    val intent = context.packageManager
                        .getLaunchIntentForPackage("com.luntik.terminal")
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                } catch (_: Exception) { }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        GlassCard(
            title = "Приложения",
            subtitle = "Скоро здесь появятся LuntikAi, Lumina и другие.",
            actionLabel = null,
            onAction = null
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "v0.1.0  ·  LuntikVisuals",
            color = G.TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GlassCard(
    title: String,
    subtitle: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(1.dp, G.Border, shape)
            .padding(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(21.dp))
                .background(Color(0xFF14141C).copy(alpha = 0.72f))
                .padding(20.dp)
        ) {
            Text(
                text = title,
                color = G.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = G.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp),
                lineHeight = 18.sp
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(G.Accent)
                        .clickable { onAction() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = actionLabel,
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

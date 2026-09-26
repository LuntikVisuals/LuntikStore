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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent { StoreRoot() }
    }
}

private object G {
    val Bg0 = Color(0xFF08080C)
    val Bg1 = Color(0xFF101018)
    val Bg2 = Color(0xFF16161F)
    val GlassStrong = Color.White.copy(alpha = 0.11f)
    val Border = Color.White.copy(alpha = 0.13f)
    val Text = Color(0xFFF2F2F7)
    val TextDim = Color.White.copy(alpha = 0.55f)
    val TextMute = Color.White.copy(alpha = 0.32f)
    val Accent = Color(0xFF8B9CFF)
    val Green = Color(0xFF5CFFB0)
    val Warning = Color(0xFFFFC857)
}

@Composable
fun StoreRoot() {
    var selectedId by remember { mutableStateOf<String?>(null) }
    val app = selectedId?.let { Catalog.byId(it) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(G.Bg0, G.Bg1, G.Bg0)))
    ) {
        Box(
            Modifier
                .size(280.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(listOf(G.Accent.copy(alpha = 0.14f), Color.Transparent)),
                    CircleShape
                )
        )

        if (app == null) {
            CatalogScreen(onOpen = { selectedId = it })
        } else {
            DetailScreen(app = app, onBack = { selectedId = null })
        }
    }
}

@Composable
private fun CatalogScreen(onOpen: (String) -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            Spacer(Modifier.height(28.dp))
            Text("LuntikStore", color = G.Text, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("магазин · отзывы · проверка LuntikAi", color = G.TextDim, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
        }

        items(Catalog.apps) { app ->
            val installed = ApkDownloader.isInstalled(context, app.packageName)
            AppRow(app, installed) { onOpen(app.id) }
            Spacer(Modifier.height(12.dp))
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text(
                "v0.2.0 · APK с GitHub Releases · отзывы локально",
                color = G.TextMute,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AppRow(app: CatalogApp, installed: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f))
                )
            )
            .border(1.dp, G.Border, shape)
            .clickable(onClick = onClick)
            .padding(1.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .background(G.Bg2.copy(alpha = 0.88f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(app.accent)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(app.name, color = G.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(app.tagline, color = G.TextDim, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("v${app.version}", color = G.TextMute, fontSize = 11.sp)
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (installed) G.Green.copy(alpha = 0.15f) else G.GlassStrong)
                    .border(
                        1.dp,
                        if (installed) G.Green.copy(alpha = 0.4f) else G.Border,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    if (installed) "установлено" else "открыть",
                    color = if (installed) G.Green else G.TextDim,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AppIcon(accent: Color) {
    Box(
        Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.15f))
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(2.dp, accent, RoundedCornerShape(6.dp))
        )
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accent)
        )
    }
}

@Composable
private fun DetailScreen(app: CatalogApp, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reviewStore = remember { ReviewStore(context) }

    var reviewList by remember { mutableStateOf(reviewStore.getReviews(app.id)) }
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("Гость") }

    var downloading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }

    val safety = remember(app.id) { LuntikAiSafety.analyze(app) }
    val installed = ApkDownloader.isInstalled(context, app.packageName)

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                "←  Назад",
                color = G.Accent,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app.accent)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(app.name, color = G.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(app.tagline, color = G.TextDim, fontSize = 14.sp)
                    Text("v${app.version}", color = G.TextMute, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(app.description, color = G.TextDim, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(20.dp))

            val btnLabel = when {
                downloading -> "Загрузка ${(progress * 100).toInt()}%"
                installed -> "Открыть"
                else -> "Скачать и установить"
            }
            PrimaryButton(btnLabel, enabled = !downloading) {
                if (installed) {
                    ApkDownloader.openApp(context, app.packageName)
                } else {
                    if (!ApkDownloader.canInstall(context)) {
                        status = "Нужно разрешение на установку APK"
                        ApkDownloader.openInstallSettings(context)
                        return@PrimaryButton
                    }
                    downloading = true
                    status = "Скачивание с GitHub..."
                    scope.launch {
                        val result = ApkDownloader.download(
                            context,
                            app.downloadUrl,
                            "${app.id}.apk"
                        ) { progress = it }
                        withContext(Dispatchers.Main) {
                            downloading = false
                            if (result.success && result.file != null) {
                                status = "Установка..."
                                ApkDownloader.install(context, result.file)
                                status = "Диалог установки открыт"
                            } else {
                                status = result.error ?: "Ошибка"
                            }
                        }
                    }
                }
            }

            status?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = G.TextMute, fontSize = 12.sp)
            }

            Spacer(Modifier.height(28.dp))
            SectionLabel("LUNTIKAI · БЕЗОПАСНОСТЬ")
            Spacer(Modifier.height(10.dp))
            GlassBlock {
                Text(
                    safety.title,
                    color = if (safety.safe) G.Green else G.Warning,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Оценка ${safety.score}/100",
                    color = G.TextMute,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    safety.summary,
                    color = G.TextDim,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 18.sp
                )
                safety.points.forEach { p ->
                    Text(
                        "·  $p",
                        color = G.TextDim,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp),
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            SectionLabel("ОТЗЫВЫ")
            Spacer(Modifier.height(10.dp))

            GlassBlock {
                Text("Оставить отзыв", color = G.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { star ->
                        Text(
                            if (star <= rating) "★" else "☆",
                            color = if (star <= rating) G.Warning else G.TextMute,
                            fontSize = 22.sp,
                            modifier = Modifier.clickable { rating = star }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Field(author, "Имя") { author = it }
                Spacer(Modifier.height(8.dp))
                Field(reviewText, "Текст отзыва") { reviewText = it }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Отправить") {
                    if (reviewText.isNotBlank()) {
                        reviewStore.addReview(
                            Review(
                                appId = app.id,
                                author = author.ifBlank { "Гость" },
                                rating = rating,
                                text = reviewText.trim()
                            )
                        )
                        reviewList = reviewStore.getReviews(app.id)
                        reviewText = ""
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (reviewList.isEmpty()) {
                Text("Пока нет отзывов — будь первым.", color = G.TextMute, fontSize = 13.sp)
            } else {
                val avg = reviewStore.averageRating(app.id)
                Text(
                    "Средняя оценка: ${"%.1f".format(avg)} · ${reviewList.size}",
                    color = G.TextDim,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        items(reviewList) { r ->
            GlassBlock {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(r.author, color = G.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(8.dp))
                    Text("★".repeat(r.rating), color = G.Warning, fontSize = 12.sp)
                }
                Text(
                    r.text,
                    color = G.TextDim,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp),
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionLabel(t: String) {
    Text(
        t,
        color = G.TextMute,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun GlassBlock(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f))
                )
            )
            .border(1.dp, G.Border, shape)
            .padding(1.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(17.dp))
                .background(G.Bg2.copy(alpha = 0.9f))
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) G.Accent else G.Accent.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Field(value: String, hint: String, onChange: (String) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, G.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        if (value.isEmpty()) {
            Text(hint, color = G.TextMute, fontSize = 13.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = G.Text, fontSize = 13.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

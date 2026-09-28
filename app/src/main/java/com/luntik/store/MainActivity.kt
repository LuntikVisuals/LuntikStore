package com.luntik.store

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        UpdateNotifier.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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
    val Error = Color(0xFFFF6B7A)
}

private enum class Screen { Auth, Catalog, Detail }

@Composable
fun StoreRoot() {
    val context = LocalContext.current
    val accountStore = remember { AccountStore(context) }
    var screen by remember {
        mutableStateOf(if (accountStore.isLoggedIn()) Screen.Catalog else Screen.Auth)
    }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var refreshTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTick) {
        val outdated = mutableListOf<String>()
        coroutineScope {
            Catalog.apps.map { app ->
                async {
                    if (!ApkDownloader.isInstalled(context, app.packageName)) return@async
                    val remote = ReleaseChecker.fetchLatest(app)
                    if (remote.available &&
                        ReleaseChecker.hasUpdate(context, app.id, remote.publishedAt)
                    ) {
                        outdated.add(app.name)
                    }
                }
            }.forEach { it.await() }
        }
        if (outdated.isNotEmpty()) UpdateNotifier.notifyUpdates(context, outdated)
    }

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

        when {
            screen == Screen.Auth -> AuthScreen(accountStore) { screen = Screen.Catalog }
            selectedId != null -> {
                val app = Catalog.byId(selectedId!!)
                if (app != null) {
                    DetailScreen(
                        app = app,
                        onBack = { selectedId = null; refreshTick++ },
                        onChanged = { refreshTick++ }
                    )
                } else selectedId = null
            }
            else -> {
                var tab by remember { mutableStateOf(MainTab.Apps) }
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        when (tab) {
                            MainTab.Apps -> CatalogScreen(
                                refreshTick = refreshTick,
                                account = accountStore.current(),
                                onOpen = { selectedId = it },
                                onLogout = {
                                    accountStore.setSession(false)
                                    screen = Screen.Auth
                                }
                            )
                            MainTab.Profile -> ProfileTab(
                                account = accountStore.current(),
                                onLogout = {
                                    accountStore.setSession(false)
                                    screen = Screen.Auth
                                }
                            )
                        }
                    }
                    StoreBottomBar(tab = tab, onTab = { tab = it })
                }
            }
        }
    }
}

@Composable
private fun AuthScreen(accountStore: AccountStore, onDone: () -> Unit) {
    var isRegister by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var display by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("LuntikStore", color = G.Text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(if (isRegister) "Регистрация" else "Вход", color = G.TextDim, fontSize = 15.sp)
        Spacer(Modifier.height(20.dp))
        Field("Логин", username) { username = it }
        Spacer(Modifier.height(10.dp))
        if (isRegister) {
            Field("Имя", display) { display = it }
            Spacer(Modifier.height(10.dp))
        }
        Field("Пароль", password, password = true) { password = it }
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = G.Error, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(if (isRegister) "Создать аккаунт" else "Войти") {
            error = if (isRegister) accountStore.register(username, password, display)
            else accountStore.login(username, password)
            if (error == null) onDone()
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (isRegister) "Уже есть аккаунт — войти" else "Нет аккаунта — регистрация",
            color = G.Accent,
            fontSize = 13.sp,
            modifier = Modifier.clickable { isRegister = !isRegister; error = null }
        )
    }
}

@Composable
private fun Field(label: String, value: String, password: Boolean = false, onChange: (String) -> Unit) {
    Text(label, color = G.TextMute, fontSize = 12.sp)
    Spacer(Modifier.height(4.dp))
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(G.GlassStrong)
            .border(1.dp, G.Border, RoundedCornerShape(12.dp)).padding(14.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = G.Text, fontSize = 15.sp),
            cursorBrush = SolidColor(G.Accent),
            singleLine = true,
            visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CatalogScreen(
    refreshTick: Int,
    account: Account?,
    onOpen: (String) -> Unit,
    onLogout: () -> Unit
) {
    var cat by remember { mutableStateOf(AppCategory.ALL) }
    @Suppress("UNUSED_VARIABLE") val t = refreshTick
    val list = Catalog.byCategory(cat)

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("LuntikStore", color = G.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(account?.displayName ?: "", color = G.TextDim, fontSize = 12.sp)
            }
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppCategory.entries.forEach { c ->
                val sel = cat == c
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (sel) G.Accent else G.GlassStrong)
                        .border(1.dp, G.Border, RoundedCornerShape(20.dp))
                        .clickable { cat = c }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(c.title, color = if (sel) Color.Black else G.Text, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(list, key = { it.id }) { app ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(G.GlassStrong)
                        .border(1.dp, G.Border, RoundedCornerShape(16.dp))
                        .clickable { onOpen(app.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(app.accent.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(app.name.take(1), color = app.accent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.name, color = G.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(app.tagline, color = G.TextDim, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            if (app.isOfficial) app.category.title else "Автор: ${app.author}",
                            color = G.TextMute,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailScreen(app: CatalogApp, onBack: () -> Unit, onChanged: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reviewStore = remember { ReviewStore(context) }
    var reviewList by remember { mutableStateOf(reviewStore.getReviews(app.id)) }
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var downloading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var remote by remember { mutableStateOf<RemoteRelease?>(null) }
    var checking by remember { mutableStateOf(true) }
    val installed = ApkDownloader.isInstalled(context, app.packageName)

    LaunchedEffect(app.id) {
        checking = true
        remote = withContext(Dispatchers.IO) { ReleaseChecker.fetchLatest(app) }
        checking = false
    }

    fun startDownload() {
        if (app.apkAssetName.isBlank() && remote?.downloadUrl.isNullOrBlank()) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.repoUrl + "/releases/latest")))
            status = "Открыты Releases на GitHub — скачай APK там"
            return
        }
        if (!ApkDownloader.canInstall(context)) {
            status = "Нужно разрешение на установку APK"
            ApkDownloader.openInstallSettings(context)
            return
        }
        val url = remote?.downloadUrl ?: app.downloadUrlFallback
        downloading = true
        status = "Скачивание..."
        scope.launch {
            val result = ApkDownloader.download(context, url, "${app.id}.apk") { progress = it }
            withContext(Dispatchers.Main) {
                downloading = false
                status = if (result.ok) {
                    ApkDownloader.install(context, result.file!!)
                    "Установка..."
                } else result.error ?: "Ошибка"
                onChanged()
            }
        }
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("← Назад", color = G.Accent, fontSize = 14.sp, modifier = Modifier.clickable(onClick = onBack))
        Spacer(Modifier.height(12.dp))
        Text(app.name, color = G.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(app.tagline, color = G.TextDim, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Text(app.description, color = G.TextDim, fontSize = 14.sp, lineHeight = 20.sp)
        if (!app.isOfficial) {
            Spacer(Modifier.height(8.dp))
            Text("Автор: ${app.author}", color = G.Accent, fontSize = 13.sp)
            Text(app.repoUrl, color = G.TextMute, fontSize = 11.sp)
        }
        Spacer(Modifier.height(20.dp))
        when {
            downloading -> PrimaryButton("Загрузка ${(progress * 100).toInt()}%") {}
            !installed -> PrimaryButton("Скачать и установить") { startDownload() }
            else -> {
                PrimaryButton("Открыть") {
                    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
                        context.startActivity(it)
                    }
                }
                Spacer(Modifier.height(8.dp))
                PrimaryButton("Обновить / переустановить") { startDownload() }
            }
        }
        status?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = G.TextDim, fontSize = 12.sp)
        }
        if (checking) {
            Spacer(Modifier.height(6.dp))
            Text("Проверка релизов...", color = G.TextMute, fontSize = 12.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text("Отзывы", color = G.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        reviewList.forEach { r ->
            Text("${r.author}: ${r.rating}/5 — ${r.text}", color = G.TextDim, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.height(12.dp))
        Field("Ваш отзыв", reviewText) { reviewText = it }
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Отправить отзыв") {
            if (reviewText.isNotBlank()) {
                reviewStore.add(app.id, accountStoreName = "user", rating, reviewText)
                reviewList = reviewStore.getReviews(app.id)
                reviewText = ""
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(G.Accent)
            .clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

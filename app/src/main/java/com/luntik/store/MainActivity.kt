package com.luntik.store

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import java.io.File

class MainActivity : ComponentActivity() {
    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        unlockHighRefreshRate()
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

    private fun unlockHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                val modes = display?.supportedModes ?: return
                val best = modes.maxByOrNull { it.refreshRate } ?: return
                val lp = window.attributes
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    lp.preferredDisplayModeId = best.modeId
                }
                window.attributes = lp
                window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            }
        } catch (_: Exception) { }
    }
}

private enum class Screen { Auth, Catalog, Detail }

@Composable
fun StoreRoot() {
    val context = LocalContext.current
    val accountStore = remember { AccountStore(context) }
    val settings = remember { SettingsStore(context) }
    val wishlist = remember { WishlistStore(context) }
    var screen by remember {
        mutableStateOf(if (accountStore.isLoggedIn()) Screen.Catalog else Screen.Auth)
    }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var updatesChecked by remember { mutableStateOf(false) }
    var wishTick by remember { mutableIntStateOf(0) }

    val accent = settings.accentColor()
    val isLight = settings.theme == ThemeMode.LIGHT
    val bg0 = if (isLight) Color(0xFFF4F4F8) else Color(0xFF08080C)
    val bg1 = if (isLight) Color(0xFFE8E8F0) else Color(0xFF101018)
    val text = if (isLight) Color(0xFF12121A) else Color(0xFFF2F2F7)
    val textDim = if (isLight) Color(0xFF555566) else Color.White.copy(alpha = 0.55f)
    val glass = if (isLight) Color.Black.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.11f)
    val border = if (isLight) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.13f)

    val wallBmp = remember(settings.wallpaperPath) {
        settings.wallpaperPath?.let { p ->
            try {
                val f = File(p)
                if (f.exists()) BitmapFactory.decodeFile(p) else null
            } catch (_: Exception) { null }
        }
    }

    LaunchedEffect(screen) {
        if (screen != Screen.Catalog || updatesChecked) return@LaunchedEffect
        updatesChecked = true
        val outdated = mutableListOf<String>()
        withContext(Dispatchers.IO) {
            coroutineScope {
                Catalog.apps.map { app ->
                    async {
                        if (!ApkDownloader.isInstalled(context, app.packageName)) return@async
                        val remote = ReleaseChecker.fetchLatest(app)
                        if (remote.available &&
                            ReleaseChecker.hasUpdate(context, app.id, remote.publishedAt)
                        ) {
                            synchronized(outdated) { outdated.add(app.name) }
                        }
                    }
                }.forEach { it.await() }
            }
        }
        if (outdated.isNotEmpty()) UpdateNotifier.notifyUpdates(context, outdated)
    }

    Box(Modifier.fillMaxSize()) {
        if (wallBmp != null) {
            Image(
                bitmap = wallBmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (isLight) 0.35f else 0.55f)))
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(bg0, bg1, bg0)))
            )
        }

        when {
            screen == Screen.Auth -> AuthScreen(accountStore, accent, text, textDim, glass, border) {
                accountStore.setSession(true)
                screen = Screen.Catalog
            }
            selectedId != null -> {
                val app = Catalog.byId(selectedId!!)
                if (app != null) {
                    DetailScreen(
                        app = app,
                        accent = accent,
                        text = text,
                        textDim = textDim,
                        glass = glass,
                        border = border,
                        wishlist = wishlist,
                        wishTick = wishTick,
                        onWish = { wishTick++ },
                        onBack = { selectedId = null }
                    )
                } else selectedId = null
            }
            else -> {
                var tab by remember { mutableStateOf(MainTab.Apps) }
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        when (tab) {
                            MainTab.Apps -> CatalogScreen(
                                account = accountStore.current(),
                                settings = settings,
                                wishlist = wishlist,
                                wishTick = wishTick,
                                accent = accent,
                                text = text,
                                textDim = textDim,
                                glass = glass,
                                border = border,
                                onOpen = { selectedId = it },
                                onWish = { wishTick++ }
                            )
                            MainTab.Library -> LibraryScreen(
                                wishlist = wishlist,
                                wishTick = wishTick,
                                accent = accent,
                                text = text,
                                textDim = textDim,
                                glass = glass,
                                border = border,
                                onOpen = { selectedId = it },
                                onWish = { wishTick++ }
                            )
                            MainTab.Settings -> SettingsScreen(
                                settings = settings,
                                accountStore = accountStore,
                                accent = accent
                            )
                            MainTab.Profile -> ProfileTab(
                                account = accountStore.current(),
                                onLogout = {
                                    accountStore.setSession(false)
                                    screen = Screen.Auth
                                },
                                accent = accent
                            )
                        }
                    }
                    StoreBottomBar(tab = tab, onTab = { tab = it }, accent = accent)
                }
            }
        }
    }
}

@Composable
private fun AuthScreen(
    accountStore: AccountStore,
    accent: Color,
    text: Color,
    textDim: Color,
    glass: Color,
    border: Color,
    onDone: () -> Unit
) {
    var isRegister by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var display by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("LuntikStore", color = text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(if (isRegister) "Регистрация" else "Вход", color = textDim, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Если тебе нет совершеннолетия — прочитай соглашение с родителями/опекунами.",
            color = textDim.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))
        Field("Логин", username, text, glass, border, accent) { username = it }
        Spacer(Modifier.height(10.dp))
        if (isRegister) {
            Field("Имя", display, text, glass, border, accent) { display = it }
            Spacer(Modifier.height(10.dp))
        }
        Field("Пароль", password, text, glass, border, accent, password = true) { password = it }
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color(0xFFFF6B7A), fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(if (isRegister) "Создать аккаунт" else "Войти", accent) {
            error = if (isRegister) accountStore.register(username, password, display)
            else accountStore.login(username, password)
            if (error == null) {
                accountStore.setSession(true)
                onDone()
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (isRegister) "Уже есть аккаунт — войти" else "Нет аккаунта — регистрация",
            color = accent,
            fontSize = 13.sp,
            modifier = Modifier.clickable { isRegister = !isRegister; error = null }
        )
    }
}

@Composable
private fun Field(
    label: String, value: String, text: Color, glass: Color, border: Color, accent: Color,
    password: Boolean = false, onChange: (String) -> Unit
) {
    Text(label, color = text.copy(alpha = 0.4f), fontSize = 12.sp)
    Spacer(Modifier.height(4.dp))
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass)
            .border(1.dp, border, RoundedCornerShape(12.dp)).padding(14.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = text, fontSize = 15.sp),
            cursorBrush = SolidColor(accent),
            singleLine = true,
            visualTransformation = if (password) PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CatalogScreen(
    account: Account?,
    settings: SettingsStore,
    wishlist: WishlistStore,
    wishTick: Int,
    accent: Color,
    text: Color,
    textDim: Color,
    glass: Color,
    border: Color,
    onOpen: (String) -> Unit,
    onWish: () -> Unit
) {
    val context = LocalContext.current
    val reviewStore = remember { ReviewStore(context) }
    var cat by remember { mutableStateOf(AppCategory.ALL) }
    val list = Catalog.byCategory(cat)
    val promo = remember { PromoCalendar.activePromoPercent() }

    @Suppress("UNUSED_VARIABLE")
    val tick = wishTick

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("LuntikStore", color = text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(account?.displayName ?: "", color = textDim, fontSize = 12.sp)
            }
            promo?.let { (pct, name) ->
                Text("$name −$pct%", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (!settings.hideAds) {
            val ads = Catalog.featured()
            if (ads.isNotEmpty()) {
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ads.forEach { app ->
                        Box(
                            Modifier
                                .width(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(app.accent.copy(alpha = 0.22f))
                                .border(1.dp, border, RoundedCornerShape(14.dp))
                                .clickable { onOpen(app.id) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Реклама", color = textDim, fontSize = 10.sp)
                                Text(app.name, color = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(app.tagline, color = textDim, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
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
                        .background(if (sel) accent else glass)
                        .border(1.dp, border, RoundedCornerShape(20.dp))
                        .clickable { cat = c }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(c.title, color = if (sel) Color.Black else text, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        val cols = settings.grid.columns
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(list, key = { it.id }) { app ->
                val avg = reviewStore.averageRating(app.id)
                val cnt = reviewStore.reviewCount(app.id)
                val wished = wishlist.isWished(app.id)
                val wDisc = wishlist.wishlistDiscountPercent(app.id)
                val best = PromoCalendar.bestDiscount(wDisc)
                AppCard(
                    app = app,
                    avg = avg,
                    cnt = cnt,
                    wished = wished,
                    discount = best,
                    accent = accent,
                    text = text,
                    textDim = textDim,
                    glass = glass,
                    border = border,
                    compact = cols > 1,
                    onOpen = { onOpen(app.id) },
                    onToggleWish = {
                        wishlist.toggle(app.id)
                        onWish()
                    }
                )
            }
        }
    }
}

@Composable
private fun AppCard(
    app: CatalogApp,
    avg: Float,
    cnt: Int,
    wished: Boolean,
    discount: Pair<Int, String>,
    accent: Color,
    text: Color,
    textDim: Color,
    glass: Color,
    border: Color,
    compact: Boolean,
    onOpen: () -> Unit,
    onToggleWish: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glass)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .padding(if (compact) 10.dp else 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(if (compact) 36.dp else 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(app.accent.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(app.name.take(1), color = app.accent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            Text(
                if (wished) "★" else "☆",
                color = if (wished) Color(0xFFFFC857) else textDim,
                fontSize = 20.sp,
                modifier = Modifier.clickable { onToggleWish() }
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(app.name, color = text, fontSize = if (compact) 13.sp else 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!compact) {
            Text(app.tagline, color = textDim, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(4.dp))
        StarRow(rating = avg, count = cnt, compact = true, star = Color(0xFFFFC857), mute = textDim)
        if (app.price > 0 && discount.first > 0) {
            val finalPrice = (app.price * (100 - discount.first) / 100)
            Text("${discount.second} −${discount.first}% · $finalPrice", color = accent, fontSize = 11.sp)
        } else if (app.price > 0) {
            Text("${app.price} валюты", color = textDim, fontSize = 11.sp)
        } else {
            Text("Бесплатно", color = textDim, fontSize = 11.sp)
        }
    }
}

@Composable
private fun LibraryScreen(
    wishlist: WishlistStore,
    wishTick: Int,
    accent: Color,
    text: Color,
    textDim: Color,
    glass: Color,
    border: Color,
    onOpen: (String) -> Unit,
    onWish: () -> Unit
) {
    @Suppress("UNUSED_VARIABLE")
    val tick = wishTick
    val items = wishlist.all().mapNotNull { (id, _) -> Catalog.byId(id)?.let { it to wishlist.wishlistDiscountPercent(id) } }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Text("Библиотека", color = text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Желаемое и скидки за ожидание", color = textDim, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        if (items.isEmpty()) {
            Text("Список желаемого пуст — нажми ☆ у приложения", color = textDim, fontSize = 14.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.first.id }) { (app, disc) ->
                    val days = wishlist.daysInWishlist(app.id)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(glass)
                            .border(1.dp, border, RoundedCornerShape(14.dp))
                            .clickable { onOpen(app.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(app.name, color = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("$days дн. в желаемом" +
                                if (disc > 0) " · скидка $disc%" else "",
                                color = if (disc > 0) accent else textDim,
                                fontSize = 12.sp
                            )
                        }
                        Text("★", color = Color(0xFFFFC857), fontSize = 20.sp,
                            modifier = Modifier.clickable {
                                wishlist.toggle(app.id)
                                onWish()
                            })
                    }
                }
            }
        }
    }
}

@Composable
private fun StarRow(rating: Float, count: Int, compact: Boolean = false, star: Color, mute: Color) {
    val full = rating.toInt().coerceIn(0, 5)
    val label = if (count == 0) "Нет оценок" else String.format("%.1f", rating) + " · $count"
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            buildString {
                repeat(full) { append('★') }
                repeat(5 - full) { append('☆') }
            },
            color = if (count == 0) mute else star,
            fontSize = if (compact) 11.sp else 16.sp
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = mute, fontSize = if (compact) 10.sp else 12.sp)
    }
}

@Composable
private fun StarPicker(value: Int, onChange: (Int) -> Unit, star: Color, mute: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { i ->
            Text(
                if (i <= value) "★" else "☆",
                color = if (i <= value) star else mute,
                fontSize = 28.sp,
                modifier = Modifier.clickable { onChange(i) }
            )
        }
    }
}

@Composable
private fun DetailScreen(
    app: CatalogApp,
    accent: Color,
    text: Color,
    textDim: Color,
    glass: Color,
    border: Color,
    wishlist: WishlistStore,
    wishTick: Int,
    onWish: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reviewStore = remember { ReviewStore(context) }
    val accountName = remember { AccountStore(context).current()?.username ?: "Игрок" }
    var reviewList by remember { mutableStateOf(reviewStore.getReviews(app.id)) }
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var downloading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var remote by remember { mutableStateOf<RemoteRelease?>(null) }
    var checking by remember { mutableStateOf(true) }
    val installed = remember { ApkDownloader.isInstalled(context, app.packageName) }
    @Suppress("UNUSED_VARIABLE")
    val tick = wishTick
    val wished = wishlist.isWished(app.id)
    val wDisc = wishlist.wishlistDiscountPercent(app.id)
    val best = PromoCalendar.bestDiscount(wDisc)

    val avg = reviewStore.averageRating(app.id)
    val cnt = reviewList.size

    LaunchedEffect(app.id) {
        checking = true
        remote = withContext(Dispatchers.IO) { ReleaseChecker.fetchLatest(app) }
        checking = false
    }

    fun startDownload() {
        when (app.installSource) {
            InstallSource.PLAY -> {
                val pkg = app.playPackage ?: app.packageName
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                    )
                } catch (_: Exception) {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                        )
                    )
                }
                status = "Открыт Google Play"
                return
            }
            InstallSource.OFFICIAL_SITE -> {
                val url = app.officialDownloadUrl ?: app.repoUrl
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                status = "Открыт официальный сайт"
                return
            }
            else -> {}
        }
        if (app.apkAssetName.isBlank() && remote?.downloadUrl.isNullOrBlank()) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.repoUrl + "/releases/latest")))
            status = "Открыты Releases на GitHub"
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
                status = if (result.success) {
                    result.file?.let { ApkDownloader.install(context, it) }
                    "Установка..."
                } else result.error ?: "Ошибка"
            }
        }
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("← Назад", color = accent, fontSize = 14.sp, modifier = Modifier.clickable(onClick = onBack))
            Text(
                if (wished) "★ В желаемом" else "☆ В желаемое",
                color = if (wished) Color(0xFFFFC857) else textDim,
                fontSize = 13.sp,
                modifier = Modifier.clickable {
                    wishlist.toggle(app.id)
                    onWish()
                }
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(app.name, color = text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(app.tagline, color = textDim, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        StarRow(avg, cnt, star = Color(0xFFFFC857), mute = textDim)
        if (app.price > 0) {
            Spacer(Modifier.height(6.dp))
            if (best.first > 0) {
                val fp = app.price * (100 - best.first) / 100
                Text("Цена: $fp (−${best.first}% ${best.second})", color = accent, fontSize = 14.sp)
            } else {
                Text("Цена: ${app.price} валюты Wallet", color = textDim, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(app.description, color = textDim, fontSize = 14.sp, lineHeight = 20.sp)
        if (!app.isOfficial) {
            Spacer(Modifier.height(8.dp))
            Text("Права / автор: ${app.author}", color = accent, fontSize = 13.sp)
            Text(
                when (app.installSource) {
                    InstallSource.PLAY -> "Источник: Google Play"
                    InstallSource.OFFICIAL_SITE -> "Источник: официальный сайт"
                    else -> app.repoUrl
                },
                color = textDim, fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        val btnLabel = when {
            downloading -> "Загрузка ${(progress * 100).toInt()}%"
            app.installSource == InstallSource.PLAY -> "Открыть в Play"
            app.installSource == InstallSource.OFFICIAL_SITE -> "Сайт разработчика"
            !installed -> "Скачать и установить"
            else -> "Обновить / переустановить"
        }
        PrimaryButton(btnLabel, accent) { if (!downloading) startDownload() }
        if (installed && app.installSource != InstallSource.PLAY) {
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Открыть", accent) {
                context.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
                    context.startActivity(it)
                }
            }
        }
        status?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = textDim, fontSize = 12.sp)
        }
        if (checking) {
            Spacer(Modifier.height(6.dp))
            Text("Проверка релизов...", color = textDim, fontSize = 12.sp)
        }

        Spacer(Modifier.height(28.dp))
        Text("Оставить оценку", color = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        StarPicker(rating, { rating = it }, Color(0xFFFFC857), textDim)
        Spacer(Modifier.height(10.dp))
        Field("Комментарий", reviewText, text, glass, border, accent) { reviewText = it }
        Spacer(Modifier.height(10.dp))
        PrimaryButton("Отправить оценку", accent) {
            reviewStore.addReview(
                Review(app.id, accountName, rating, reviewText.ifBlank { "Без комментария" })
            )
            reviewList = reviewStore.getReviews(app.id)
            reviewText = ""
            status = "Оценка сохранена: $rating★"
        }
        Spacer(Modifier.height(24.dp))
        Text("Отзывы (${reviewList.size})", color = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (reviewList.isEmpty()) {
            Text("Пока нет отзывов", color = textDim, fontSize = 13.sp)
        } else {
            reviewList.forEach { r ->
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass).padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(r.author, color = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(8.dp))
                        Text(buildString { repeat(r.rating) { append('★') } }, color = Color(0xFFFFC857), fontSize = 12.sp)
                    }
                    if (r.text.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(r.text, color = textDim, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun PrimaryButton(text: String, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(accent)
            .clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

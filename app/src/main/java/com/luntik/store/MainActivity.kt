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
    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        unlockHighRefreshRate()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        UpdateNotifier.ensureChannel(this)
        DownloadCenter.init(this)
        LocalFolders.ensureStructure(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent { StoreRoot() }
    }

    private fun unlockHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val d = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) this.display
                else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                val best = d?.supportedModes?.maxByOrNull { it.refreshRate } ?: return
                val lp = window.attributes
                lp.preferredDisplayModeId = best.modeId
                window.attributes = lp
                window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            }
        } catch (_: Exception) { }
    }
}

private enum class Screen { Auth, Main, Detail, Cart, Downloads }

@Composable
fun StoreRoot() {
    val context = LocalContext.current
    val accountStore = remember { AccountStore(context) }
    val settings = remember { SettingsStore(context) }
    val wishlist = remember { WishlistStore(context) }
    val cart = remember { CartStore(context) }
    val personalCode = remember { PersonalCodeStore(context) }

    var screen by remember { mutableStateOf(if (accountStore.isLoggedIn()) Screen.Main else Screen.Auth) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var updatesChecked by remember { mutableStateOf(false) }
    var cloudUnlocked by remember {
        mutableStateOf(!settings.cloudPasswordEnabled || !accountStore.isLoggedIn())
    }

    val accent = settings.accentColor()
    val isLight = settings.theme == ThemeMode.LIGHT
    val bg0 = if (isLight) Color(0xFFF4F4F8) else Color(0xFF08080C)
    val bg1 = if (isLight) Color(0xFFE8E8F0) else Color(0xFF101018)
    val text = if (isLight) Color(0xFF12121A) else Color(0xFFF2F2F7)
    val textDim = if (isLight) Color(0xFF555566) else Color.White.copy(alpha = 0.55f)
    val glass = if (isLight) Color.Black.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.11f)
    val border = if (isLight) Color.Black.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.13f)

    SideEffect {
        val w = (context as? ComponentActivity)?.window ?: return@SideEffect
        WindowInsetsControllerCompat(w, w.decorView).apply {
            isAppearanceLightStatusBars = isLight
            isAppearanceLightNavigationBars = isLight
        }
    }

    val wallBmp = remember(settings.wallpaperPath) {
        settings.wallpaperPath?.let { p ->
            try { if (File(p).exists()) BitmapFactory.decodeFile(p) else null } catch (_: Exception) { null }
        }
    }

    LaunchedEffect(screen) {
        if (screen != Screen.Main || updatesChecked) return@LaunchedEffect
        updatesChecked = true
        val outdated = mutableListOf<String>()
        withContext(Dispatchers.IO) {
            coroutineScope {
                Catalog.apps.map { app ->
                    async {
                        if (!ApkDownloader.isInstalled(context, app.packageName)) return@async
                        val remote = ReleaseChecker.fetchLatest(app)
                        if (remote.available && ReleaseChecker.hasUpdate(context, app.id, remote.publishedAt))
                            synchronized(outdated) { outdated.add(app.name) }
                    }
                }.forEach { it.await() }
            }
        }
        if (outdated.isNotEmpty()) UpdateNotifier.notifyUpdates(context, outdated)
    }

    Box(Modifier.fillMaxSize()) {
        if (wallBmp != null) {
            Image(bitmap = wallBmp.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (isLight) 0.4f else 0.55f)))
        } else {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(bg0, bg1, bg0))))
        }

        when {
            screen == Screen.Auth -> AuthScreen(accountStore, accent, text, textDim, glass, border) {
                accountStore.setSession(true)
                LocalFolders.ensureStructure(context)
                accountStore.current()?.let { LocalFolders.writeProfile(context, it.username, it.displayName) }
                cloudUnlocked = !settings.cloudPasswordEnabled
                screen = Screen.Main
            }
            accountStore.isLoggedIn() && settings.cloudPasswordEnabled && !cloudUnlocked -> {
                CloudLockScreen(accountStore, accent, text, textDim, glass, border) { cloudUnlocked = true }
            }
            selectedId != null -> {
                val app = Catalog.byId(selectedId!!)
                if (app != null) DetailScreen(app, accent, text, textDim, glass, border, wishlist, cart) { selectedId = null }
                else selectedId = null
            }
            screen == Screen.Cart -> CartScreen(cart, accent, text, textDim, glass, border,
                onBack = { screen = Screen.Main }, onOpen = { selectedId = it }, onGoDownloads = { screen = Screen.Downloads })
            screen == Screen.Downloads -> DownloadsScreen(accent, text, textDim, glass, border) { screen = Screen.Main }
            else -> {
                var tab by remember { mutableStateOf(MainTab.Apps) }
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val n = cart.ids().size.also { cart.version }
                        Row(
                            Modifier.clip(RoundedCornerShape(12.dp)).clickable { screen = Screen.Cart }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconCart(color = accent, size = 22.dp)
                            if (n > 0) {
                                Spacer(Modifier.width(6.dp))
                                Text("$n", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        Box(Modifier.clip(RoundedCornerShape(12.dp)).clickable { screen = Screen.Downloads }.padding(8.dp)) {
                            IconDownloadUp(color = textDim, size = 22.dp)
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        when (tab) {
                            MainTab.Apps -> CatalogScreen(accountStore.current(), settings, wishlist, accent, text, textDim, glass, border) { selectedId = it }
                            MainTab.Library -> LibraryScreen(wishlist, accent, text, textDim, glass, border) { selectedId = it }
                            MainTab.Settings -> SettingsScreen(settings, accountStore, personalCode, accent, text, textDim, glass, border, isLight)
                            MainTab.Profile -> ProfileTab(accountStore.current(), onLogout = {
                                accountStore.setSession(false); cloudUnlocked = false; screen = Screen.Auth
                            }, accent = accent)
                        }
                    }
                    StoreBottomBar(tab, { tab = it }, accent)
                }
            }
        }
    }
}

@Composable
private fun CloudLockScreen(
    accountStore: AccountStore, accent: Color, text: Color, textDim: Color, glass: Color, border: Color, onOk: () -> Unit
) {
    var pw by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Облачный пароль", color = text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Введи пароль аккаунта", color = textDim, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Field("Пароль", pw, text, glass, border, accent, password = true) { pw = it }
        err?.let { Spacer(Modifier.height(8.dp)); Text(it, color = Color(0xFFFF6B7A), fontSize = 13.sp) }
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Разблокировать", accent) {
            if (accountStore.verifyPassword(pw)) onOk() else err = "Неверный пароль"
        }
    }
}

@Composable
private fun AuthScreen(
    accountStore: AccountStore, accent: Color, text: Color, textDim: Color, glass: Color, border: Color, onDone: () -> Unit
) {
    var isRegister by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var display by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf(accountStore.hasAcceptedTerms()) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("LuntikStore", color = text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(if (isRegister) "Регистрация" else "Вход", color = textDim, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        Text("Если тебе нет совершеннолетия — прочитай соглашение с родителями/опекунами.", color = textDim, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        Field("Логин", username, text, glass, border, accent) { username = it }
        Spacer(Modifier.height(10.dp))
        if (isRegister) {
            Field("Имя", display, text, glass, border, accent) { display = it }
            Spacer(Modifier.height(10.dp))
        }
        Field("Пароль", password, text, glass, border, accent, password = true) { password = it }
        if (isRegister) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().clickable { terms = !terms }, verticalAlignment = Alignment.Top) {
                Text(if (terms) "☑" else "☐", color = accent, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text("Принимаю соглашение. Аккаунт — лицензия, не собственность. Разработчик не отвечает за вирусы.", color = textDim, fontSize = 11.sp)
            }
        }
        error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = Color(0xFFFF6B7A), fontSize = 13.sp) }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(if (isRegister) "Создать аккаунт" else "Войти", accent) {
            if (isRegister) {
                if (terms) accountStore.acceptTerms()
                error = accountStore.register(username, password, display)
            } else error = accountStore.login(username, password)
            if (error == null) { accountStore.setSession(true); onDone() }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (isRegister) "Уже есть аккаунт — войти" else "Нет аккаунта — регистрация",
            color = accent, fontSize = 13.sp,
            modifier = Modifier.clickable { isRegister = !isRegister; error = null }
        )
    }
}

@Composable
private fun Field(
    label: String, value: String, text: Color, glass: Color, border: Color, accent: Color,
    password: Boolean = false, onChange: (String) -> Unit
) {
    Text(label, color = text.copy(alpha = 0.45f), fontSize = 12.sp)
    Spacer(Modifier.height(4.dp))
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass).border(1.dp, border, RoundedCornerShape(12.dp)).padding(14.dp)) {
        BasicTextField(
            value = value, onValueChange = onChange,
            textStyle = TextStyle(color = text, fontSize = 15.sp),
            cursorBrush = SolidColor(accent), singleLine = true,
            visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CatalogScreen(
    account: Account?, settings: SettingsStore, wishlist: WishlistStore,
    accent: Color, text: Color, textDim: Color, glass: Color, border: Color, onOpen: (String) -> Unit
) {
    val context = LocalContext.current
    val reviewStore = remember { ReviewStore(context) }
    var cat by remember { mutableStateOf(AppCategory.ALL) }
    val list = Catalog.byCategory(cat)
    val promo = remember { PromoCalendar.activePromoPercent() }
    val wishVer = wishlist.version
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("LuntikStore", color = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${account?.displayName ?: ""} · ${list.size} приложений", color = textDim, fontSize = 12.sp)
            }
            promo?.let { (pct, name) -> Text("$name −$pct%", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
        if (!settings.hideAds) {
            val ads = Catalog.featured()
            if (ads.isNotEmpty()) {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ads.forEach { app ->
                        Box(Modifier.width(140.dp).clip(RoundedCornerShape(12.dp)).background(app.accent.copy(alpha = 0.2f)).border(1.dp, border, RoundedCornerShape(12.dp)).clickable { onOpen(app.id) }.padding(10.dp)) {
                            Column {
                                Text("Реклама", color = textDim, fontSize = 10.sp)
                                Text(app.name, color = text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCategory.entries.forEach { c ->
                val sel = cat == c
                Box(Modifier.clip(RoundedCornerShape(20.dp)).background(if (sel) accent else glass).border(1.dp, border, RoundedCornerShape(20.dp)).clickable { cat = c }.padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(c.title, color = if (sel) Color.Black else text, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        val cols = settings.grid.columns
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(list, key = { it.id + "_$wishVer" }) { app ->
                Box(Modifier.fillMaxWidth().height(if (cols >= 3) 148.dp else if (cols == 2) 150.dp else 120.dp)) {
                    AppCard(
                        app, reviewStore.averageRating(app.id), reviewStore.reviewCount(app.id),
                        wishlist.isWished(app.id), PromoCalendar.bestDiscount(wishlist.wishlistDiscountPercent(app.id)),
                        accent, text, textDim, glass, border, cols > 1,
                        onOpen = { onOpen(app.id) }, onToggleWish = { wishlist.toggle(app.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppCard(
    app: CatalogApp, avg: Float, cnt: Int, wished: Boolean, discount: Pair<Int, String>,
    accent: Color, text: Color, textDim: Color, glass: Color, border: Color, compact: Boolean,
    onOpen: () -> Unit, onToggleWish: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(glass)
            .border(1.dp, border, RoundedCornerShape(14.dp)).clickable(onClick = onOpen)
            .padding(if (compact) 8.dp else 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(if (compact) 28.dp else 40.dp).clip(RoundedCornerShape(10.dp)).background(app.accent.copy(alpha = 0.28f)), contentAlignment = Alignment.Center) {
                Text(app.name.take(1), color = app.accent, fontWeight = FontWeight.Bold, fontSize = if (compact) 12.sp else 16.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(if (wished) "★" else "☆", color = if (wished) Color(0xFFFFC857) else textDim, fontSize = if (compact) 18.sp else 22.sp, modifier = Modifier.clickable { onToggleWish() }.padding(2.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(app.name, color = text, fontSize = if (compact) 12.sp else 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.weight(1f))
        StarRow(avg, cnt, true, Color(0xFFFFC857), textDim)
        Text(
            when {
                app.price > 0 && discount.first > 0 -> "${app.price * (100 - discount.first) / 100} (−${discount.first}%)"
                app.price > 0 -> "${app.price} валюты"
                else -> "Бесплатно"
            }, color = textDim, fontSize = 10.sp, maxLines = 1
        )
    }
}

@Composable
private fun LibraryScreen(
    wishlist: WishlistStore, accent: Color, text: Color, textDim: Color, glass: Color, border: Color, onOpen: (String) -> Unit
) {
    val context = LocalContext.current
    val wishVer = wishlist.version
    val installed = remember { Catalog.apps.filter { ApkDownloader.isInstalled(context, it.packageName) } }
    val wished = wishlist.all().mapNotNull { Catalog.byId(it.first) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text("Библиотека", color = text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Установленные (${installed.size})", color = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (installed.isEmpty()) Text("Пока ничего не установлено", color = textDim, fontSize = 13.sp)
        }
        items(installed, key = { it.id }) { app ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass).border(1.dp, border, RoundedCornerShape(12.dp)).clickable { onOpen(app.id) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(app.name, color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("v${ApkDownloader.installedVersionName(context, app.packageName) ?: "?"}", color = textDim, fontSize = 12.sp)
                }
                Text("Открыть", color = accent, fontSize = 13.sp, modifier = Modifier.clickable { ApkDownloader.openApp(context, app.packageName) })
            }
            Spacer(Modifier.height(8.dp))
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("Желаемое (${wished.size})", color = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (wished.isEmpty()) Text("Нажми ☆ в каталоге", color = textDim, fontSize = 13.sp)
            @Suppress("UNUSED_VARIABLE") val _v = wishVer
        }
        items(wished, key = { it.id }) { app ->
            val days = wishlist.daysInWishlist(app.id)
            val disc = wishlist.wishlistDiscountPercent(app.id)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass).border(1.dp, border, RoundedCornerShape(12.dp)).clickable { onOpen(app.id) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(app.name, color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("$days дн." + if (disc > 0) " · −$disc%" else "", color = if (disc > 0) accent else textDim, fontSize = 12.sp)
                }
                Text("★", color = Color(0xFFFFC857), fontSize = 20.sp, modifier = Modifier.clickable { wishlist.toggle(app.id) })
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CartScreen(
    cart: CartStore, accent: Color, text: Color, textDim: Color, glass: Color, border: Color,
    onBack: () -> Unit, onOpen: (String) -> Unit, onGoDownloads: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ver = cart.version
    val apps = cart.ids().mapNotNull { Catalog.byId(it) }
    var status by remember { mutableStateOf<String?>(null) }

    fun downloadApp(app: CatalogApp) {
        when (app.installSource) {
            InstallSource.PLAY -> {
                val pkg = app.playPackage ?: app.packageName
                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))) }
                catch (_: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))) }
                status = "Play: ${app.name}"; cart.remove(app.id)
            }
            InstallSource.OFFICIAL_SITE -> {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.officialDownloadUrl ?: app.repoUrl)))
                status = "Сайт: ${app.name}"; cart.remove(app.id)
            }
            else -> {
                if (!ApkDownloader.canInstall(context)) {
                    status = "Нужно разрешение на установку APK"
                    ApkDownloader.openInstallSettings(context); return
                }
                scope.launch {
                    val remote = withContext(Dispatchers.IO) { ReleaseChecker.fetchLatest(app) }
                    val url = remote.downloadUrl ?: app.downloadUrlFallback
                    if (app.apkAssetName.isBlank() && remote.downloadUrl.isNullOrBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.repoUrl + "/releases/latest")))
                        status = "Releases: ${app.name}"; return@launch
                    }
                    DownloadCenter.startGithubDownload(context, app, url) { cart.remove(app.id) }
                    status = "Загрузка ${app.name}"
                    onGoDownloads()
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Text("← Назад", color = accent, modifier = Modifier.clickable(onClick = onBack))
        Spacer(Modifier.height(8.dp))
        Text("Корзина", color = text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Скачивание только отсюда. Прогресс — в Загрузках.", color = textDim, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        if (apps.isEmpty()) Text("Пусто — добавь «В корзину»", color = textDim, fontSize = 14.sp)
        else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(apps, key = { it.id + ver }) { app ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass).border(1.dp, border, RoundedCornerShape(12.dp)).padding(12.dp)) {
                        Text(app.name, color = text, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onOpen(app.id) })
                        Text(if (app.price > 0) "${app.price} валюты" else "Бесплатно", color = textDim, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { PrimaryButton("Скачать", accent) { downloadApp(app) } }
                            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).border(1.dp, border, RoundedCornerShape(12.dp)).clickable { cart.remove(app.id) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text("Убрать", color = Color(0xFFFF6B7A), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Скачать всё", accent) { apps.forEach { downloadApp(it) } }
        }
        status?.let { Spacer(Modifier.height(8.dp)); Text(it, color = textDim, fontSize = 12.sp) }
    }
}

@Composable
private fun DownloadsScreen(
    accent: Color, text: Color, textDim: Color, glass: Color, border: Color, onBack: () -> Unit
) {
    val items = DownloadCenter.items
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Text("← Назад", color = accent, modifier = Modifier.clickable(onClick = onBack))
        Spacer(Modifier.height(8.dp))
        Text("Загрузки", color = text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Не сбрасываются при уходе с экрана", color = textDim, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) Text("Пока пусто — качай из корзины", color = textDim, fontSize = 14.sp)
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.appId + it.status + it.progress }) { d ->
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass).border(1.dp, border, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Text(d.name, color = text, fontWeight = FontWeight.Bold)
                    Text(d.status + (d.error?.let { " · $it" } ?: ""), color = textDim, fontSize = 12.sp)
                    Text("Откуда: ${d.fromUrl.take(56)}", color = textDim, fontSize = 11.sp)
                    if (d.toPath.isNotBlank()) Text("Куда: ${d.toPath}", color = textDim, fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(border)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(d.progress.coerceIn(0f, 1f)).background(accent))
                    }
                    Text("${(d.progress * 100).toInt()}%", color = accent, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StarRow(rating: Float, count: Int, compact: Boolean, star: Color, mute: Color) {
    val full = rating.toInt().coerceIn(0, 5)
    val label = if (count == 0) "—" else String.format("%.1f", rating)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(buildString { repeat(full) { append('★') }; repeat(5 - full) { append('☆') } }, color = if (count == 0) mute else star, fontSize = if (compact) 10.sp else 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(label, color = mute, fontSize = if (compact) 10.sp else 12.sp)
    }
}

@Composable
private fun DetailScreen(
    app: CatalogApp, accent: Color, text: Color, textDim: Color, glass: Color, border: Color,
    wishlist: WishlistStore, cart: CartStore, onBack: () -> Unit
) {
    val context = LocalContext.current
    val reviewStore = remember { ReviewStore(context) }
    val accountName = remember { AccountStore(context).current()?.username ?: "Игрок" }
    var reviewList by remember { mutableStateOf(reviewStore.getReviews(app.id)) }
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var showAv by remember { mutableStateOf(false) }
    val installed = remember { ApkDownloader.isInstalled(context, app.packageName) }
    val wished = wishlist.isWished(app.id).also { wishlist.version }
    val best = PromoCalendar.bestDiscount(wishlist.wishlistDiscountPercent(app.id))
    val av = remember(app.id) { AntivirusScanner.scan(app) }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("← Назад", color = accent, modifier = Modifier.clickable(onClick = onBack))
            Text(if (wished) "★ В желаемом" else "☆ В желаемое", color = if (wished) Color(0xFFFFC857) else textDim, modifier = Modifier.clickable { wishlist.toggle(app.id) })
        }
        Spacer(Modifier.height(12.dp))
        Text(app.name, color = text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(app.tagline, color = textDim, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        StarRow(reviewStore.averageRating(app.id), reviewList.size, false, Color(0xFFFFC857), textDim)
        if (app.price > 0) {
            val fp = if (best.first > 0) app.price * (100 - best.first) / 100 else app.price
            Text(if (best.first > 0) "Цена: $fp (−${best.first}% ${best.second})" else "Цена: $fp валюты", color = accent, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(app.description, color = textDim, fontSize = 14.sp, lineHeight = 20.sp)
        if (!app.isOfficial) { Spacer(Modifier.height(8.dp)); Text("Права: ${app.author}", color = accent, fontSize = 13.sp) }
        Spacer(Modifier.height(12.dp))
        Text("Антивирус: ${av.level}", color = accent, fontSize = 13.sp, modifier = Modifier.clickable { showAv = !showAv })
        if (showAv) {
            Spacer(Modifier.height(6.dp))
            Text("Разрешения: ${av.permissions.joinToString()}", color = textDim, fontSize = 12.sp)
            av.risks.forEach { Text("• $it", color = textDim, fontSize = 12.sp) }
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(if (cart.contains(app.id)) "Уже в корзине" else "В корзину", accent) {
            cart.add(app.id); status = "В корзине — скачивай оттуда"
        }
        if (installed) {
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Открыть приложение", glass) { ApkDownloader.openApp(context, app.packageName) }
        }
        status?.let { Spacer(Modifier.height(8.dp)); Text(it, color = textDim, fontSize = 12.sp) }
        Spacer(Modifier.height(24.dp))
        Text("Оценка", color = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { i ->
                Text(if (i <= rating) "★" else "☆", color = if (i <= rating) Color(0xFFFFC857) else textDim, fontSize = 28.sp, modifier = Modifier.clickable { rating = i })
            }
        }
        Spacer(Modifier.height(10.dp))
        Field("Комментарий", reviewText, text, glass, border, accent) { reviewText = it }
        Spacer(Modifier.height(10.dp))
        PrimaryButton("Отправить", accent) {
            reviewStore.addReview(Review(app.id, accountName, rating, reviewText.ifBlank { "Без комментария" }))
            reviewList = reviewStore.getReviews(app.id); reviewText = ""; status = "Оценка сохранена"
        }
        Spacer(Modifier.height(16.dp))
        reviewList.forEach { r ->
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass).padding(12.dp)) {
                Text("${r.author} ${"★".repeat(r.rating)}", color = text, fontSize = 13.sp)
                Text(r.text, color = textDim, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun PrimaryButton(label: String, bg: Color, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg).clickable(onClick = onClick).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
        Text(label, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

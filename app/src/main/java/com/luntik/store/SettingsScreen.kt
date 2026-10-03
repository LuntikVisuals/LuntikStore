package com.luntik.store

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

private enum class SettingsSection { Privacy, Personal, Integrations }

@Composable
fun SettingsScreen(
    settings: SettingsStore,
    accountStore: AccountStore,
    accent: Color,
    text: Color,
    textDim: Color,
    glass: Color,
    border: Color
) {
    val context = LocalContext.current
    var section by remember { mutableStateOf(SettingsSection.Personal) }
    var status by remember { mutableStateOf<String?>(null) }

    val pickWall = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val out = File(context.filesDir, "store_wallpaper.jpg")
                out.outputStream().use { input.copyTo(it) }
                settings.updateWallpaper(out.absolutePath)
                status = "Фон обновлён"
            }
        } catch (_: Exception) {
            status = "Не удалось сохранить фон"
        }
    }

    Row(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(
            Modifier
                .width(120.dp)
                .fillMaxHeight()
                .background(glass)
                .padding(8.dp)
        ) {
            Text("Настройки", color = text, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp))
            SectionItem("Конфиденц.", section == SettingsSection.Privacy, accent, textDim) {
                section = SettingsSection.Privacy
            }
            SectionItem("Персонализ.", section == SettingsSection.Personal, accent, textDim) {
                section = SettingsSection.Personal
            }
            SectionItem("Интеграции", section == SettingsSection.Integrations, accent, textDim) {
                section = SettingsSection.Integrations
            }
        }

        Column(
            Modifier.weight(1f).fillMaxHeight()
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            when (section) {
                SettingsSection.Privacy -> PrivacyPanel(
                    accountStore, settings, accent, text, textDim, glass, border
                ) { status = it }
                SettingsSection.Personal -> PersonalPanel(
                    settings, accent, text, textDim, glass, border,
                    filesDir = context.filesDir,
                    onPickWall = { pickWall.launch(arrayOf("image/*")) },
                    onStatus = { status = it }
                )
                SettingsSection.Integrations -> {
                    Text("Привязка аккаунтов к играм", color = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Скоро: привязка профиля Luntik к играм. Пока в разработке.",
                        color = textDim, fontSize = 13.sp
                    )
                }
            }
            status?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = accent, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SectionItem(
    title: String, selected: Boolean, accent: Color, textDim: Color, onClick: () -> Unit
) {
    Text(
        title,
        color = if (selected) accent else textDim,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(10.dp)
    )
}

@Composable
private fun PrivacyPanel(
    accountStore: AccountStore,
    settings: SettingsStore,
    accent: Color,
    text: Color,
    textDim: Color,
    glass: Color,
    border: Color,
    onStatus: (String) -> Unit
) {
    var newNick by remember { mutableStateOf(accountStore.current()?.displayName ?: "") }
    var oldPw by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }

    Text("Конфиденциальность", color = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Text("Смена ника", color = textDim, fontSize = 13.sp)
    Spacer(Modifier.height(6.dp))
    SettingsField(newNick, text, glass, border, accent) { newNick = it }
    Spacer(Modifier.height(8.dp))
    SettingsBtn("Сохранить ник", accent) {
        onStatus(accountStore.updateDisplayName(newNick) ?: "Ник обновлён")
    }

    Spacer(Modifier.height(20.dp))
    Text("Смена пароля", color = textDim, fontSize = 13.sp)
    Spacer(Modifier.height(6.dp))
    SettingsField(oldPw, text, glass, border, accent, "Текущий пароль") { oldPw = it }
    Spacer(Modifier.height(6.dp))
    SettingsField(newPw, text, glass, border, accent, "Новый пароль") { newPw = it }
    Spacer(Modifier.height(8.dp))
    SettingsBtn("Сменить пароль", accent) {
        val err = accountStore.changePassword(oldPw, newPw)
        onStatus(err ?: "Пароль изменён")
        if (err == null) { oldPw = ""; newPw = "" }
    }

    Spacer(Modifier.height(20.dp))
    Row(
        Modifier.fillMaxWidth().clickable {
            settings.updateCloudPasswordEnabled(!settings.cloudPasswordEnabled)
            onStatus(
                if (!settings.cloudPasswordEnabled) "Облачный пароль выключен"
                else "Облачный пароль включён — спросит при входе"
            )
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (settings.cloudPasswordEnabled) "☑" else "☐", color = accent, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text("Облачный пароль при входе", color = text, fontSize = 14.sp)
            Text("Запрашивать пароль при открытии Store", color = textDim, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PersonalPanel(
    settings: SettingsStore,
    accent: Color,
    text: Color,
    textDim: Color,
    glass: Color,
    border: Color,
    filesDir: File,
    onPickWall: () -> Unit,
    onStatus: (String) -> Unit
) {
    Text("Персонализация", color = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))

    Text("Тема", color = textDim, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { m ->
            val sel = settings.theme == m
            Box(
                Modifier.clip(RoundedCornerShape(10.dp))
                    .background(if (sel) accent else glass)
                    .border(1.dp, border, RoundedCornerShape(10.dp))
                    .clickable { settings.updateTheme(m) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(m.title, color = if (sel) Color.Black else text, fontSize = 13.sp)
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    Text("Акцентный цвет", color = textDim, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))
    val presets = listOf(
        Color(0xFF8B9CFF), Color(0xFF5CFFB0), Color(0xFFFF6B7A),
        Color(0xFFFFC857), Color(0xFF4FC3F7), Color(0xFFFFB8E0)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        presets.forEach { c ->
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(c)
                    .border(2.dp, if (settings.accentArgb == c.value.toInt()) text else Color.Transparent, CircleShape)
                    .clickable { settings.updateAccent(c); onStatus("Акцент обновлён") }
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Text("Сетка каталога", color = textDim, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GridMode.entries.forEach { g ->
            val sel = settings.grid == g
            Box(
                Modifier.clip(RoundedCornerShape(10.dp))
                    .background(if (sel) accent else glass)
                    .border(1.dp, border, RoundedCornerShape(10.dp))
                    .clickable { settings.updateGrid(g) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(g.title, color = if (sel) Color.Black else text, fontSize = 12.sp)
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text("Превью:", color = textDim, fontSize = 11.sp)
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(settings.grid.columns) {
            Box(
                Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.35f))
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Text("Фон приложения", color = textDim, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))
    SettingsBtn("Выбрать фото", accent, onClick = onPickWall)
    Spacer(Modifier.height(6.dp))
    SettingsBtn("Сбросить фон", glass) {
        settings.updateWallpaper(null)
        File(filesDir, "store_wallpaper.jpg").delete()
        onStatus("Фон сброшен")
    }

    Spacer(Modifier.height(16.dp))
    Row(
        Modifier.fillMaxWidth().clickable { settings.updateHideAds(!settings.hideAds) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (settings.hideAds) "☑" else "☐", color = accent, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text("Скрыть рекламу приложений", color = text, fontSize = 14.sp)
            Text("Премиум-настройка (локально)", color = textDim, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingsField(
    value: String, text: Color, glass: Color, border: Color, accent: Color,
    hint: String = "", onChange: (String) -> Unit
) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(glass)
            .border(1.dp, border, RoundedCornerShape(12.dp)).padding(12.dp)
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = androidx.compose.ui.text.TextStyle(color = text, fontSize = 14.sp),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(accent),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty() && hint.isNotEmpty()) {
                    Text(hint, color = text.copy(alpha = 0.35f), fontSize = 14.sp)
                }
                inner()
            }
        )
    }
}

@Composable
private fun SettingsBtn(textLabel: String, bg: Color, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg)
            .clickable(onClick = onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(textLabel, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

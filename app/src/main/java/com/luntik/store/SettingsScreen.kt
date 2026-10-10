package com.luntik.store

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

private enum class SettingsSection { Privacy, Personal, Integrations, Code }

@Composable
fun SettingsScreen(settings: SettingsStore, accountStore: AccountStore, personalCode: PersonalCodeStore, accent: Color, text: Color, textDim: Color, glass: Color, border: Color, isLight: Boolean) {
    val context = LocalContext.current
    var section by remember { mutableStateOf(SettingsSection.Personal) }
    var status by remember { mutableStateOf<String?>(null) }
    val friends = remember { LocalFolders.listFriends(context) }
    val folderPath = remember { LocalFolders.rootPath(context) }
    val pickWall = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
        try { context.contentResolver.openInputStream(uri)?.use { input -> val out = File(context.filesDir, "store_wallpaper.jpg"); out.outputStream().use { input.copyTo(it) }; settings.updateWallpaper(out.absolutePath); status = "Фон обновлён" } } catch (_: Exception) { status = "Не удалось сохранить фон" }
    }
    val importFriend = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try { val tmp = File(context.cacheDir, "friend_import.json"); context.contentResolver.openInputStream(uri)?.use { input -> tmp.outputStream().use { input.copyTo(it) } }; status = LocalFolders.importFriendProfile(context, tmp) ?: "Друг добавлен" } catch (e: Exception) { status = e.message ?: "Ошибка импорта" }
    }
    Row(Modifier.fillMaxSize().statusBarsPadding().padding(10.dp)) {
        Column(Modifier.width(124.dp).fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(Color.White.copy(0.08f)).border(1.dp, Color.White.copy(0.14f), RoundedCornerShape(24.dp)).padding(vertical = 12.dp, horizontal = 6.dp)) {
            Text("Настройки", color = text, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
            SectionChip("Конфиденц.", section == SettingsSection.Privacy, accent, textDim) { section = SettingsSection.Privacy }
            SectionChip("Внешний вид", section == SettingsSection.Personal, accent, textDim) { section = SettingsSection.Personal }
            SectionChip("Код", section == SettingsSection.Code, accent, textDim) { section = SettingsSection.Code }
            SectionChip("Друзья", section == SettingsSection.Integrations, accent, textDim) { section = SettingsSection.Integrations }
        }
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(12.dp)) {
            AnimatedContent(section, transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(140)) }, label = "set") { s ->
                when (s) {
                    SettingsSection.Privacy -> PrivacyPanel(accountStore, settings, accent, text, textDim, glass, border) { status = it }
                    SettingsSection.Personal -> PersonalPanel(settings, accent, text, textDim, glass, border, context.filesDir, { pickWall.launch(arrayOf("image/*")) }) { status = it }
                    SettingsSection.Code -> Column { Text("Персональный код", color = text, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("Никому не показывай.", color = textDim, fontSize = 12.sp); Spacer(Modifier.height(12.dp)); Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(glass).border(1.dp, border, RoundedCornerShape(16.dp)).padding(14.dp)) { Text(personalCode.code, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.height(10.dp)); SettingsBtn("Заменить код", accent) { personalCode.regenerate(); status = "Код обновлён" } }
                    SettingsSection.Integrations -> Column { Text("Друзья", color = text, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(folderPath, color = accent, fontSize = 11.sp); Spacer(Modifier.height(10.dp)); SettingsBtn("Создать папки", accent) { LocalFolders.ensureStructure(context); status = "Папки готовы" }; Spacer(Modifier.height(8.dp)); SettingsBtn("Импорт друга", accent) { importFriend.launch(arrayOf("application/json", "*/*")) }; Spacer(Modifier.height(12.dp)); if (friends.isEmpty()) Text("Пока нет друзей", color = textDim) else friends.forEach { f -> Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(glass).padding(12.dp)) { Text(f.displayName, color = text, fontWeight = FontWeight.Bold); Text("@${f.username}", color = textDim, fontSize = 12.sp) } } }
                }
            }
            status?.let { Spacer(Modifier.height(12.dp)); Text(it, color = accent, fontSize = 13.sp) }
        }
    }
}
@Composable private fun SectionChip(title: String, selected: Boolean, accent: Color, textDim: Color, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 3.dp).clip(RoundedCornerShape(16.dp)).background(if (selected) accent else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 10.dp)) { Text(title, color = if (selected) Color.Black else textDim, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
}
@Composable private fun PrivacyPanel(accountStore: AccountStore, settings: SettingsStore, accent: Color, text: Color, textDim: Color, glass: Color, border: Color, onStatus: (String) -> Unit) {
    var newNick by remember { mutableStateOf(accountStore.current()?.displayName ?: "") }
    var oldPw by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    Column { Text("Конфиденциальность", color = text, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp))
        CardBlock(glass, border) { Text("Смена ника", color = textDim, fontSize = 12.sp); SettingsField(newNick, text, glass, border, accent) { newNick = it }; SettingsBtn("Сохранить ник", accent) { onStatus(accountStore.updateDisplayName(newNick) ?: "Ник обновлён") } }
        Spacer(Modifier.height(12.dp))
        CardBlock(glass, border) { Text("Смена пароля", color = textDim, fontSize = 12.sp); SettingsField(oldPw, text, glass, border, accent, "текущий") { oldPw = it }; SettingsField(newPw, text, glass, border, accent, "новый") { newPw = it }; SettingsBtn("Сменить пароль", accent) { val err = accountStore.changePassword(oldPw, newPw); onStatus(err ?: "Пароль изменён"); if (err == null) { oldPw = ""; newPw = "" } } }
        Spacer(Modifier.height(12.dp))
        CardBlock(glass, border) { Row(Modifier.fillMaxWidth().clickable { settings.updateCloudPasswordEnabled(!settings.cloudPasswordEnabled); onStatus(if (settings.cloudPasswordEnabled) "вкл" else "выкл") }, verticalAlignment = Alignment.CenterVertically) { Text(if (settings.cloudPasswordEnabled) "☑" else "☐", color = accent, fontSize = 18.sp); Spacer(Modifier.width(8.dp)); Text("Облачный пароль при входе", color = text) } }
    }
}
@Composable private fun PersonalPanel(settings: SettingsStore, accent: Color, text: Color, textDim: Color, glass: Color, border: Color, filesDir: File, onPickWall: () -> Unit, onStatus: (String) -> Unit) {
    Column { Text("Внешний вид", color = text, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp))
        CardBlock(glass, border) { Text("Тема", color = textDim, fontSize = 12.sp); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ThemeMode.entries.forEach { m -> val sel = settings.theme == m; Box(Modifier.clip(RoundedCornerShape(16.dp)).background(if (sel) accent else glass).clickable { settings.updateTheme(m) }.padding(horizontal = 14.dp, vertical = 8.dp)) { Text(m.title, color = if (sel) Color.Black else text) } } } }
        Spacer(Modifier.height(12.dp))
        CardBlock(glass, border) { Text("Акцент", color = textDim, fontSize = 12.sp); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) { listOf(Color(0xFF8B9CFF), Color(0xFF5CFFB0), Color(0xFFFF6B7A), Color(0xFFFFC857), Color(0xFF4FC3F7), Color(0xFFFFB8E0)).forEach { c -> Box(Modifier.size(32.dp).clip(CircleShape).background(c).clickable { settings.updateAccent(c); onStatus("акцент") }) } } }
        Spacer(Modifier.height(12.dp))
        CardBlock(glass, border) { Text("Сетка", color = textDim, fontSize = 12.sp); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { GridMode.entries.forEach { g -> val sel = settings.grid == g; Box(Modifier.clip(RoundedCornerShape(16.dp)).background(if (sel) accent else glass).clickable { settings.updateGrid(g) }.padding(horizontal = 10.dp, vertical = 8.dp)) { Text(g.title, color = if (sel) Color.Black else text, fontSize = 12.sp) } } } }
        Spacer(Modifier.height(12.dp))
        CardBlock(glass, border) { SettingsBtn("Выбрать фото", accent, onPickWall); Spacer(Modifier.height(6.dp)); SettingsBtn("Сбросить фон", glass) { settings.updateWallpaper(null); File(filesDir, "store_wallpaper.jpg").delete(); onStatus("фон сброшен") } }
        Spacer(Modifier.height(12.dp))
        CardBlock(glass, border) { Row(Modifier.fillMaxWidth().clickable { settings.updateHideAds(!settings.hideAds) }) { Text(if (settings.hideAds) "☑" else "☐", color = accent, fontSize = 18.sp); Spacer(Modifier.width(8.dp)); Text("Скрыть рекламу", color = text) } }
    }
}
@Composable private fun CardBlock(glass: Color, border: Color, content: @Composable ColumnScope.() -> Unit) { Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(glass).border(1.dp, border, RoundedCornerShape(20.dp)).padding(14.dp), content = content) }
@Composable private fun SettingsField(value: String, text: Color, glass: Color, border: Color, accent: Color, hint: String = "", onChange: (String) -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp)).background(glass).border(1.dp, border, RoundedCornerShape(14.dp)).padding(12.dp)) {
        androidx.compose.foundation.text.BasicTextField(value, onChange, textStyle = androidx.compose.ui.text.TextStyle(color = text, fontSize = 14.sp), cursorBrush = SolidColor(accent), singleLine = true, modifier = Modifier.fillMaxWidth(), decorationBox = { inner -> if (value.isEmpty() && hint.isNotEmpty()) Text(hint, color = text.copy(0.35f)); inner() })
    }
}
@Composable private fun SettingsBtn(textLabel: String, bg: Color, onClick: () -> Unit) { Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(bg).clickable(onClick = onClick).padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text(textLabel, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold) } }

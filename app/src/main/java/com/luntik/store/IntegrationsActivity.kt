package com.luntik.store

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class IntegrationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { IntegrationsRoot() }
    }
}

@Composable
private fun IntegrationsRoot() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val plans = remember { PlanStore(ctx) }
    val settings = remember { SettingsStore(ctx) }
    var open by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var remote by remember { mutableStateOf(plans.remoteTag.ifBlank { "проверяю..." }) }
    val anim = settings.animations
    LaunchedEffect(Unit) {
        val app = Catalog.byId("snake") ?: return@LaunchedEffect
        val rel = ReleaseChecker.fetchLatest(app)
        remote = rel.tagName ?: rel.error ?: "не удалось проверить"
        if (!rel.tagName.isNullOrBlank()) plans.remoteTag = rel.tagName
    }
    fun pay(title: String, amount: Int, order: String, onOk: () -> Unit) {
        note = "Открыл кошелёк: $title"
        ctx.startActivity(Intent("com.luntik.wallet.PAY").setPackage("com.luntik.wallet").putExtra("amount", amount.toDouble()).putExtra("order", order).putExtra("title", title))
        onOk()
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF0E0E16)).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Интеграции", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        InfoCard("LuntikWallet", if (plans.installed("com.luntik.wallet")) "установлен" else "не установлен")
        if (!plans.installed("com.luntik.wallet")) PayRow("Скачать LuntikWallet") {
            val app = Catalog.byId("wallet") ?: return@PayRow
            DownloadCenter.startGithubDownload(ctx, app, app.downloadUrlFallback)
            note = "Качаю кошелёк"
        }
        InfoCard(plans.statusLine(), "тег GitHub: $remote")
        Text("Привязано", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        plans.linked().forEach { InfoCard(it, "установлено через Стор") }
        if (plans.perks().isNotEmpty()) InfoCard("Плюшки", plans.perks().joinToString(" · "))
        if (note.isNotBlank()) Text(note, color = Color(0xFF8B9CFF), modifier = Modifier.padding(vertical = 6.dp))
        Text("Тарифы змейки", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        SnakePlan.entries.forEach { plan ->
            ExpandCard(plan.title, "${plan.days} д · ${if (plan.battlePass) "батлпасс · " else ""}обновление ${if (plan.updatePrice == 0) "бесплатно" else "${plan.updatePrice} LC"}", open == plan.name, anim) {
                open = if (open == plan.name) "" else plan.name
            } {
                PayRow("${plan.price} LC") { pay(plan.title, plan.price, "snake-${plan.name}-${System.currentTimeMillis() / 86_400_000L}") { plans.buySnake(plan) } }
            }
        }
        Text("Подписка Стора", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        Text(if (plans.storeActive()) "сейчас ${plans.storePlan}" else "нет подписки", color = Color.White.copy(0.6f))
        listOf("Oro", "Platino", "Titano", "Argento").forEach { name ->
            val options = StorePlan.entries.filter { it.title == name }
            ExpandCard(name, "${options.size} срока", open == name, anim) { open = if (open == name) "" else name } {
                options.forEach { plan -> PayRow("${plan.days} д · ${plan.price} LC") { pay(plan.title, plan.price, "store-${plan.name}-${System.currentTimeMillis() / 86_400_000L}") { plans.buyStore(plan) } } }
            }
        }
        if (plans.tier() >= 4) {
            Text("Бесплатная игра: ${plans.freeGame.ifBlank { "не выбрана" }}", color = Color.White, modifier = Modifier.padding(top = 8.dp))
            Catalog.apps.filter { it.isOfficial && it.id != "store" }.take(6).forEach { app ->
                PayRow(app.name) { plans.freeGame = app.id; note = "Бесплатная: ${app.name}" }
            }
            PayRow("Копия профиля") { note = plans.exportBackup() }
            PayRow("Код переноса") { note = plans.makeTransfer().ifBlank { "нет активной змейки" } }
            BasicTextField(code, { code = it.take(6) }, textStyle = TextStyle(color = Color.White), cursorBrush = SolidColor(Color.White), modifier = Modifier.padding(top = 8.dp))
            PayRow("Вставить код") { note = if (plans.takeTransfer(code)) "перенесено" else "код не тот" }
        }
        Text("Чеки", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        plans.receipts().forEach { InfoCard(it, "оплата") }
        PayRow(if (settings.animations) "Анимации вкл" else "Анимации выкл") { settings.updateAnimations(!settings.animations); note = "анимации: ${settings.animations}" }
    }
}

@Composable private fun InfoCard(title: String, sub: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(0.08f)).border(1.dp, Color.White.copy(0.14f), RoundedCornerShape(16.dp)).padding(14.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Text(sub, color = Color.White.copy(0.6f), fontSize = 12.sp)
    }
}
@Composable private fun ExpandCard(title: String, sub: String, open: Boolean, anim: Boolean, onHead: () -> Unit, body: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(0.08f)).border(1.dp, Color.White.copy(0.18f), RoundedCornerShape(16.dp)).padding(14.dp)) {
        Column(Modifier.fillMaxWidth().clickable(onClick = onHead)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(sub, color = Color.White.copy(0.55f), fontSize = 12.sp)
        }
        if (anim) AnimatedVisibility(open, enter = expandVertically(tween(240)) + fadeIn(tween(180)), exit = shrinkVertically(tween(180)) + fadeOut(tween(120))) { Column(Modifier.padding(top = 8.dp)) { body() } }
        else if (open) Column(Modifier.padding(top = 8.dp)) { body() }
    }
}
@Composable private fun PayRow(label: String, on: () -> Unit) {
    Text(label, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF8B9CFF)).clickable(onClick = on).padding(12.dp))
}

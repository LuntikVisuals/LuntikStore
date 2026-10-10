package com.luntik.store

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IntegrationsActivity : ComponentActivity() {
    private var onPaid: (() -> Unit)? = null
    private val payLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> if (result.resultCode == RESULT_OK) onPaid?.invoke(); onPaid = null }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { IntegrationsRoot(::startPay) } }
    private fun startPay(title: String, amount: Int, order: String, onOk: () -> Unit) {
        onPaid = onOk
        payLauncher.launch(Intent("com.luntik.wallet.PAY").setPackage("com.luntik.wallet").putExtra("amount", amount.toDouble()).putExtra("order", order).putExtra("title", title))
    }
}
@Composable private fun IntegrationsRoot(startPay: (String, Int, String, () -> Unit) -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val plans = remember { PlanStore(ctx) }
    val settings = remember { SettingsStore(ctx) }
    var open by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var gift by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Color(0xFF0E0E16)).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Интеграции", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        InfoCard(plans.mainStatus(), "статус")
        PayRow("Обновить всё") {
            Catalog.apps.filter { it.isOfficial && plans.auto(it.id) && (it.price == 0 || plans.isFree(it.id) || (it.id == "snake" && plans.snakeActive())) }.forEach {
                DownloadCenter.startGithubDownload(ctx, it, it.downloadUrlFallback)
            }
            note = "обновляю то, что разрешено"
        }
        Text("Автообновление", color = Color.White, fontWeight = FontWeight.Bold)
        Catalog.apps.filter { it.isOfficial }.take(8).forEach { app ->
            PayRow("${app.name}: ${if (plans.auto(app.id)) "вкл" else "выкл"}") { plans.setAuto(app.id, !plans.auto(app.id)); note = app.name }
        }
        Text("Тарифы змейки", color = Color.White, fontWeight = FontWeight.Bold)
        SnakePlan.entries.forEach { plan -> ExpandCard(plan.title, "${plan.days} д", open == plan.name, settings.animations, { open = if (open == plan.name) "" else plan.name }) { PayRow("${plan.price} LC") { startPay(plan.title, plan.price, "snake-${plan.name}-${System.currentTimeMillis() / 86_400_000L}") { plans.buySnake(plan); note = "Оплачено" } }; PayRow("Подарить") { startPay(plan.title, plan.price, "gift-${plan.name}") { note = plans.makeGift(plan.name) } } } }
        Text("Подписка Стора", color = Color.White, fontWeight = FontWeight.Bold)
        listOf("Oro", "Platino", "Titano", "Argento").forEach { name ->
            val options = StorePlan.entries.filter { it.title == name }
            ExpandCard(name, "${options.size} срока", open == name, settings.animations, { open = if (open == name) "" else name }) {
                options.forEach { plan -> PayRow("${plan.days} д · ${plan.price} LC") { startPay(plan.title, plan.price, "store-${plan.name}-${System.currentTimeMillis() / 86_400_000L}") { plans.buyStore(plan); note = "Оплачено" } } }
            }
        }
        if (plans.storeActive()) {
            val extra = plans.familyPrice()
            if (!plans.familyPaid) PayRow("Семейный слот +$extra LC") { startPay("Семейный слот", extra, "family") { plans.unlockFamily(); note = "слот оплачен" } }
            else PayRow("Семейный код (макс 2)") { note = plans.familyCode().ifBlank { "слот уже занят" } }
            BasicTextField(code, { code = it }, textStyle = TextStyle(color = Color.White), cursorBrush = SolidColor(Color.White))
            PayRow("Вставить семейный") { note = if (plans.takeFamily(code)) "активировано" else "код не тот или слот занят" }
        }
        Hint(gift) { gift = it }
        PayRow("Вставить подарок") { note = if (plans.takeGift(gift)) "подарок принят" else "код не тот" }
        if (note.isNotBlank()) Text(note, color = Color(0xFF8B9CFF))
        Text("Журнал", color = Color.White, fontWeight = FontWeight.Bold)
        plans.journal().forEach { InfoCard(it, "") }
    }
}
@Composable private fun InfoCard(title: String, sub: String) { Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(0.08f)).border(1.dp, Color.White.copy(0.14f), RoundedCornerShape(16.dp)).padding(14.dp)) { Text(title, color = Color.White, fontWeight = FontWeight.Bold); if (sub.isNotBlank()) Text(sub, color = Color.White.copy(0.6f), fontSize = 12.sp) } }
@Composable private fun ExpandCard(title: String, sub: String, open: Boolean, anim: Boolean, onHead: () -> Unit, body: @Composable () -> Unit) { Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(0.08f)).padding(14.dp)) { Column(Modifier.fillMaxWidth().clickable(onClick = onHead)) { Text(title, color = Color.White, fontWeight = FontWeight.Bold); Text(sub, color = Color.White.copy(0.55f), fontSize = 12.sp) }; if (anim) AnimatedVisibility(open, enter = expandVertically(tween(240)) + fadeIn()) { Column { body() } } else if (open) Column { body() } } }
@Composable private fun PayRow(label: String, on: () -> Unit) { Text(label, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF8B9CFF)).clickable(onClick = on).padding(12.dp)) }
@Composable private fun Hint(value: String, on: (String) -> Unit) { BasicTextField(value, on, textStyle = TextStyle(color = Color.White), cursorBrush = SolidColor(Color.White), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) }

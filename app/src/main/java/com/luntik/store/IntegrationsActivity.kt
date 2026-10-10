package com.luntik.store

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
    var tick by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }
    var remote by remember { mutableStateOf(plans.remoteTag.ifBlank { "проверяю..." }) }
    val scope = rememberCoroutineScope()
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
        tick++
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF0E0E16)).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Интеграции", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        AnimatedVisibility(true, enter = slideInVertically(tween(280)) { it / 3 } + fadeIn(tween(220))) {
            Card("LuntikWallet", if (plans.installed("com.luntik.wallet")) "установлен" else "не установлен · скачать из каталога")
        }
        Card(plans.statusLine(), "тег GitHub: $remote")
        if (note.isNotBlank()) Text(note, color = Color(0xFF8B9CFF), modifier = Modifier.padding(vertical = 6.dp))
        Text("Тарифы змейки", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        SnakePlan.entries.forEach { plan ->
            PayRow("${plan.title} · ${plan.price} LC · ${plan.days} д") {
                pay(plan.title, plan.price, "snake-${plan.name}-${System.currentTimeMillis() / 86_400_000L}") { plans.buySnake(plan) }
            }
        }
        Text("Подписка Стора", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        Text(if (plans.storeActive()) "сейчас ${plans.storePlan}" else "нет подписки", color = Color.White.copy(0.6f))
        StorePlan.entries.forEach { plan ->
            PayRow("${plan.title} · ${plan.days} д · ${plan.price} LC") {
                pay(plan.title, plan.price, "store-${plan.name}-${System.currentTimeMillis() / 86_400_000L}") { plans.buyStore(plan) }
            }
        }
    }
}

@Composable
private fun Card(title: String, sub: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(0.08f)).border(1.dp, Color.White.copy(0.14f), RoundedCornerShape(16.dp)).padding(14.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Text(sub, color = Color.White.copy(0.6f), fontSize = 12.sp)
    }
}

@Composable
private fun PayRow(label: String, on: () -> Unit) {
    Text(label, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF8B9CFF)).clickable(onClick = on).padding(12.dp))
}

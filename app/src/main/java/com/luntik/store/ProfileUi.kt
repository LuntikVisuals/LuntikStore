package com.luntik.store

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val COLLAB_TG = "https://t.me/LuntikVisuals"
enum class MainTab { Apps, Library, Settings, Profile }

@Composable
fun StoreBottomBar(tab: MainTab, onTab: (MainTab) -> Unit, accent: Color = Color(0xFF8B9CFF)) {
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp)).background(Color.White.copy(0.10f))
            .border(1.dp, Color.White.copy(0.18f), RoundedCornerShape(28.dp)).padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(MainTab.Apps to "Каталог", MainTab.Library to "Библио", MainTab.Settings to "Настр", MainTab.Profile to "Профиль").forEach { (t, n) ->
            val on = tab == t
            Text(
                n,
                color = if (on) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (on) accent else Color.Transparent).clickable { onTab(t) }.padding(horizontal = 10.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
fun ProfileTab(account: Account?, onLogout: () -> Unit, accent: Color = Color(0xFF8B9CFF)) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        AnimatedVisibility(true, enter = slideInVertically(tween(280)) { it / 4 } + fadeIn(tween(220))) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White.copy(0.10f)).border(1.dp, Color.White.copy(0.16f), RoundedCornerShape(24.dp)).padding(16.dp)) {
                Text("Профиль", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(account?.displayName?.ifBlank { account.username } ?: "Гость", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("@${account?.username ?: "—"}", color = Color.White.copy(0.55f))
            }
        }
        Spacer(Modifier.height(12.dp))
        IslandBtn("Интеграции и тарифы", accent) { context.startActivity(Intent(context, IntegrationsActivity::class.java)) }
        IslandCard("Сотрудничество", "Хочешь добавить игру? Пришли GitHub в Telegram.")
        IslandBtn("Заявка в Telegram", accent) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(COLLAB_TG))) }
        IslandCard("О магазине", "LuntikStore 1.1.1 · островки и анимации")
        Spacer(Modifier.weight(1f))
        IslandBtn("Выйти", Color(0xFFFF6B7A), onLogout)
    }
}
@Composable private fun IslandCard(title: String, sub: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(0.08f)).border(1.dp, Color.White.copy(0.14f), RoundedCornerShape(22.dp)).padding(14.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Text(sub, color = Color.White.copy(0.55f), fontSize = 13.sp)
    }
}
@Composable private fun IslandBtn(label: String, bg: Color, on: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(22.dp)).background(bg).clickable(onClick = on).padding(14.dp), contentAlignment = Alignment.Center) {
        Text(label, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

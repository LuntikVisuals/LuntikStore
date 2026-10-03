package com.luntik.store

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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

private object PU {
    val Bg2 = Color(0xFF16161F)
    val Glass = Color.White.copy(alpha = 0.11f)
    val Border = Color.White.copy(alpha = 0.13f)
    val Text = Color(0xFFF2F2F7)
    val TextDim = Color.White.copy(alpha = 0.55f)
    val TextMute = Color.White.copy(alpha = 0.32f)
    val Accent = Color(0xFF8B9CFF)
    val Error = Color(0xFFFF6B7A)
}

@Composable
fun StoreBottomBar(tab: MainTab, onTab: (MainTab) -> Unit, accent: Color = PU.Accent) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PU.Bg2.copy(alpha = 0.95f))
            .border(1.dp, PU.Border)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem("Каталог", tab == MainTab.Apps, accent) { onTab(MainTab.Apps) }
        BottomItem("Библиотека", tab == MainTab.Library, accent) { onTab(MainTab.Library) }
        BottomItem("Настройки", tab == MainTab.Settings, accent) { onTab(MainTab.Settings) }
        BottomItem("Профиль", tab == MainTab.Profile, accent) { onTab(MainTab.Profile) }
    }
}

@Composable
private fun BottomItem(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            color = if (selected) accent else PU.TextMute,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun ProfileTab(account: Account?, onLogout: () -> Unit, accent: Color = PU.Accent) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Text("Профиль", color = PU.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            account?.displayName?.ifBlank { account.username } ?: "Гость",
            color = accent,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text("@${account?.username ?: "—"}", color = PU.TextDim, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PU.Glass)
                .border(1.dp, PU.Border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("Сотрудничество", color = PU.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Хочешь добавить свою игру или FOSS в каталог? Пришли ссылку на GitHub в Telegram.",
                color = PU.TextDim,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent)
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(COLLAB_TG)))
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Заявка в Telegram", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PU.Glass)
                .border(1.dp, PU.Border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("О магазине", color = PU.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "LuntikStore 0.6.0 · официальные приложения, FOSS и внешние источники с указанием прав.",
                color = PU.TextDim,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, PU.Border, RoundedCornerShape(12.dp))
                .clickable(onClick = onLogout)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Выйти", color = PU.Error, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

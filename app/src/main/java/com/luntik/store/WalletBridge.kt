package com.luntik.store

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class WalletBridge : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val code = intent.getStringExtra("code") ?: "--"
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 120, 48, 48) }
        val title = TextView(this).apply { text = "Подтверди кошелёк"; textSize = 22f }
        val body = TextView(this).apply { text = "Код из LuntikWallet: $code"; textSize = 18f }
        val ok = TextView(this).apply {
            text = "Это я"
            textSize = 20f
            setPadding(0, 40, 0, 20)
            setOnClickListener {
                startActivity(Intent("com.luntik.store.CONFIRM_WALLET").setPackage("com.luntik.wallet").putExtra("ok", true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            }
        }
        val pay = TextView(this).apply {
            text = "Оплатить змейку 300 LC"
            textSize = 20f
            setOnClickListener {
                startActivity(Intent("com.luntik.wallet.PAY").setPackage("com.luntik.wallet").putExtra("amount", 300.0).putExtra("order", "snake-rent-1").putExtra("title", "Змейка 1 день"))
            }
        }
        root.addView(title); root.addView(body); root.addView(ok); root.addView(pay)
        setContentView(root)
    }
}

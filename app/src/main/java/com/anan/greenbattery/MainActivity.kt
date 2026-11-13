package com.anan.greenbattery

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("AnAnGoodLock", MODE_PRIVATE)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#05080D"))
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(52, 100, 52, 64)
        }

        root.addView(TextView(this).apply {
            text = "AN AN GOOD LOCK"
            textSize = 26f
            setTextColor(Color.parseColor("#4CD3F7"))
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "Suite giao diện Liquid Glass & HyperOS cho One UI"
            textSize = 13f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 8, 0, 56)
        })

        root.addView(createCard(
            prefs, "quick_panel_on",
            "💠 Liquid Glass Quick Panel",
            "Vuốt góc phải trên mở bảng kính lỏng & âm lượng",
            "#4CD3F7",
            LiquidQuickPanelService::class.java
        ))
        root.addView(createCard(
            prefs, "liquid_pill_on",
            "🔮 Liquid Glass Live Pill",
            "Viên thuốc kính mờ hiển thị đồng hồ dưới camera",
            "#F25C84",
            LiquidPillService::class.java
        ))
        root.addView(createCard(
            prefs, "green_battery_on",
            "⚡ Pin Xanh HyperOS khi sạc",
            "Tự động phủ viên pin xanh lá kèm % pin khi cắm sạc",
            "#22C55E",
            ChargingBatteryService::class.java
        ))

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun createCard(
        prefs: SharedPreferences,
        key: String,
        title: String,
        desc: String,
        hex: String,
        cls: Class<*>
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 32 }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0E1724"))
                cornerRadius = 42f
                setStroke(2, Color.parseColor(hex))
            }
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
        box.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })
        box.addView(TextView(this).apply {
            text = desc
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 6, 16, 0)
        })
        val sw = Switch(this).apply {
            isChecked = prefs.getBoolean(key, false)
            setOnCheckedChangeListener { b, on ->
                if (on && !Settings.canDrawOverlays(this@MainActivity)) {
                    b.isChecked = false
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    prefs.edit().putBoolean(key, on).apply()
                    val i = Intent(this@MainActivity, cls)
                    if (on) startForegroundService(i) else stopService(i)
                }
            }
        }
        card.addView(box)
        card.addView(sw)
        return card
    }
}

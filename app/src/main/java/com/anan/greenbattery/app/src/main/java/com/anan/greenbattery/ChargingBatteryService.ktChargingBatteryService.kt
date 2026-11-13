package com.anan.greenbattery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class ChargingBatteryService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var pill: LinearLayout
    private lateinit var txt: TextView
    private var added = false

    private val rcv = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            when (i?.action) {
                Intent.ACTION_POWER_CONNECTED -> show()
                Intent.ACTION_POWER_DISCONNECTED -> hide()
                Intent.ACTION_BATTERY_CHANGED -> {
                    val st = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val chg = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL
                    val lv = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val sc = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val pct = if (lv >= 0 && sc > 0) (lv * 100 / sc) else 100
                    txt.text = "⚡ $pct%"
                    if (chg) show() else hide()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val id = "gb_ch"
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(id, "GB", NotificationManager.IMPORTANCE_MIN))
        }
        startForeground(2, Notification.Builder(this, id)
            .setContentTitle("Pin xanh sẵn sàng")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .build())

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        pill = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(18, 4, 18, 4)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#22C55E"))
                cornerRadius = 24f
                setStroke(2, Color.parseColor("#86EFAC"))
            }
        }
        txt = TextView(this).apply {
            text = "⚡ 100%"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(Color.parseColor("#052E16"))
            typeface = Typeface.DEFAULT_BOLD
        }
        pill.addView(txt)
        registerReceiver(rcv, IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        })
    }

    private fun show() {
        if (added) return
        wm.addView(pill, WindowManager.LayoutParams(
            -2, -2,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 28
            y = 18
        })
        added = true
    }

    private fun hide() {
        if (added) {
            wm.removeView(pill)
            added = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(rcv)
        hide()
    }

    override fun onBind(i: Intent?): IBinder? = null
}

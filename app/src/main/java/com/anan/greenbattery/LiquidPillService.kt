package com.anan.greenbattery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextClock

class LiquidPillService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var pill: LinearLayout

    override fun onCreate() {
        super.onCreate()
        val id = "lp_ch"
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(id, "LP", NotificationManager.IMPORTANCE_MIN))
        }
        startForeground(4, Notification.Builder(this, id)
            .setContentTitle("Liquid Pill đang chạy")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .build())

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        pill = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(36, 10, 36, 10)
            background = GradientDrawable(
                GradientDrawable.Orientation.LR,
                intArrayOf(
                    Color.parseColor("#550E1724"),
                    Color.parseColor("#442BC4F3"),
                    Color.parseColor("#44F05A7E")
                )
            ).apply {
                cornerRadius = 50f
                setStroke(2, Color.parseColor("#AA4CD3F7"))
            }
            addView(TextClock(this@LiquidPillService).apply {
                format24Hour = "HH:mm"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                setTextColor(Color.parseColor("#E6F7FF"))
                typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            })
        }
        wm.addView(pill, WindowManager.LayoutParams(
            -2, -2,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 105
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::pill.isInitialized) wm.removeView(pill)
    }

    override fun onBind(i: Intent?): IBinder? = null
}

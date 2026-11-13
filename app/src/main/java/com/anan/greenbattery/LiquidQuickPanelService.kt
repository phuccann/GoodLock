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
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class LiquidQuickPanelService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var edge: View
    private var panel: FrameLayout? = null

    override fun onCreate() {
        super.onCreate()
        val id = "qp_ch"
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(id, "QP", NotificationManager.IMPORTANCE_MIN))
        }
        startForeground(3, Notification.Builder(this, id)
            .setContentTitle("Quick Panel sẵn sàng")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .build())

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        edge = View(this).apply {
            var sy = 0f
            setOnTouchListener { _, e ->
                if (e.action == MotionEvent.ACTION_DOWN) sy = e.rawY
                if (e.action == MotionEvent.ACTION_UP && e.rawY - sy > 60) openPanel()
                true
            }
        }
        wm.addView(edge, WindowManager.LayoutParams(
            180, 90,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END })
    }

    private fun openPanel() {
        if (panel != null) return
        val bg = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#8805080D"))
            setOnClickListener { closePanel() }
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            isClickable = true
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#CC0E1724"),
                    Color.parseColor("#662BC4F3"),
                    Color.parseColor("#55F05A7E")
                )
            ).apply {
                cornerRadius = 56f
                setStroke(3, Color.parseColor("#B34CD3F7"))
            }
        }
        card.addView(TextView(this).apply {
            text = "LIQUID GLASS CONTROL"
            textSize = 14f
            setTextColor(Color.parseColor("#4CD3F7"))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 32)
        })

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 36)
        }
        row.addView(tile("📶 Wi-Fi", Settings.ACTION_WIFI_SETTINGS))
        row.addView(tile("🎧 BT", Settings.ACTION_BLUETOOTH_SETTINGS))
        row.addView(tile("⚙️ Cài đặt", Settings.ACTION_SETTINGS))
        card.addView(row)

        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        card.addView(TextView(this).apply {
            text = "🔊 Âm lượng Media"
            setTextColor(Color.WHITE)
            textSize = 13f
        })
        card.addView(SeekBar(this).apply {
            max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            progress = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                    if (u) am.setStreamVolume(AudioManager.STREAM_MUSIC, p, 0)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        bg.addView(card, FrameLayout.LayoutParams(-1, -2).apply {
            gravity = Gravity.TOP
            setMargins(36, 120, 36, 0)
        })
        wm.addView(bg, WindowManager.LayoutParams(
            -1, -1,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ))
        panel = bg
    }

    private fun tile(t: String, act: String) = TextView(this).apply {
        text = t
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        setPadding(20, 32, 20, 32)
        layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(8, 0, 8, 0) }
        background = GradientDrawable().apply {
            setColor(Color.parseColor("#442BC4F3"))
            cornerRadius = 32f
            setStroke(2, Color.parseColor("#884CD3F7"))
        }
        setOnClickListener {
            startActivity(Intent(act).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            closePanel()
        }
    }

    private fun closePanel() {
        panel?.let {
            wm.removeView(it)
            panel = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::edge.isInitialized) wm.removeView(edge)
        closePanel()
    }

    override fun onBind(i: Intent?): IBinder? = null
}

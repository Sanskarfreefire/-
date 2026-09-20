package com.tools.systeminfo7.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var panelView: View? = null
    private var espOverlayView: View? = null
    private var isPanelExpanded = false

    companion object {
        init {
            System.loadLibrary("sr_mods_engine")
        }
    }

    external fun calculateAimbot(crossX: Float, crossY: Float, targetX: Float, targetY: Float, fov: Float, smooth: Float): Long

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingBall()
        createEspOverlay()
    }

    private fun createFloatingBall() {
        val ballButton = Button(this).apply {
            text = "SR"
            setBackgroundColor(0xFF6200EE.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            120, 120,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        ballButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView ?: ballButton, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = Math.abs(event.rawX - initialTouchX)
                    val diffY = Math.abs(event.rawY - initialTouchY)
                    if (diffX < 10 && diffY < 10) {
                        togglePanel()
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = ballButton
        windowManager.addView(floatingView, params)
    }

    private fun createEspOverlay() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val espView = object : View(this) {
            private val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.GREEN
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f
            }
            override fun onDraw(canvas: android.graphics.Canvas) {
                super.onDraw(canvas)
                val cx = width / 2f
                val cy = height / 2f
                canvas.drawRect(cx - 100f, cy - 150f, cx + 100f, cy + 150f, paint)
            }
        }
        espOverlayView = espView
        windowManager.addView(espOverlayView, params)
    }

    private fun togglePanel() {
        if (isPanelExpanded) {
            panelView?.let { windowManager.removeView(it) }
            panelView = null
            isPanelExpanded = false
        } else {
            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                800, 1000,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val panelLayout = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setBackgroundColor(0xEE121212.toInt())
                setPadding(32, 32, 32, 32)
            }

            val title = TextView(this).apply {
                text = "𝐒𝐀𝐍𝐒𝐊𝐀𝐑 𝐌𝐎𝐃𝐒"
                setTextColor(0xFFFF0000.toInt())
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            panelLayout.addView(title)

            val status = TextView(this).apply {
                text = "Status: Running [Non-Root Mode]"
                setTextColor(0xFF00FF00.toInt())
                textSize = 14f
            }
            panelLayout.addView(status)

            val cbEsp = CheckBox(this).apply {
                text = "ESP Box [ON]"
                isChecked = true
                setTextColor(0xFFFFFFFF.toInt())
            }
            panelLayout.addView(cbEsp)

            val cbAimbot = CheckBox(this).apply {
                text = "Aimbot [ON]"
                isChecked = true
                setTextColor(0xFFFFFFFF.toInt())
            }
            panelLayout.addView(cbAimbot)

            val fovLabel = TextView(this).apply {
                text = "Aim FOV: 45°"
                setTextColor(0xFFFFFFFF.toInt())
            }
            panelLayout.addView(fovLabel)

            val sbFov = SeekBar(this).apply {
                max = 90
                progress = 45
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        fovLabel.text = "Aim FOV: $progress°"
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }
            panelLayout.addView(sbFov)

            val closeBtn = Button(this).apply {
                text = "Close Panel"
                setOnClickListener { togglePanel() }
            }
            panelLayout.addView(closeBtn)

            panelView = panelLayout
            windowManager.addView(panelView, params)
            isPanelExpanded = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
        panelView?.let { windowManager.removeView(it) }
        espOverlayView?.let { windowManager.removeView(it) }
    }
}

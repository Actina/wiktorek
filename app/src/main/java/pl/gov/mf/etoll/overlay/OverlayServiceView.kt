package pl.gov.mf.etoll.overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import androidx.core.content.ContextCompat
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels
import pl.gov.mf.mobile.ui.components.views.bubblewidget.NkspoWidget
import kotlin.math.roundToInt

class OverlayServiceView : OverlayServiceContract.View {
    private var windowManager: WindowManager? = null
    private var inflater: LayoutInflater? = null
    private var windowSize = Point()
    private var overlayView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var widget: NkspoWidget? = null
    private var widgetClosedView: View? = null
    private var widgetOpenView: View? = null

    private var startIntent: Intent? = null
    private var applicationContext: Context? = null

    private val defaultWindowParams: WindowManager.LayoutParams
        get() {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            params.y = dpToPixels(100)
            params.x = dpToPixels(0)
            return params
        }

    override fun initialize(context: Context) {
        applicationContext = context
        startIntent = (context.applicationContext as BaseApplication).getForegroundActionIntent()
        clearSystem()
        setupServices(context)
        setupViews(context)
    }

    override fun deinitialize() {
        clearSystem()
        applicationContext = null
    }

    override fun openOverlay() {
        overlayView?.post {
            overlayView?.let {
                widgetOpenView?.visibility = View.VISIBLE
                // check if overlay is in visible area
                it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val width = it.measuredWidth
                val height = it.measuredHeight
                params?.let { param ->
                    if (param.y + height > windowSize.y)
                        param.y = windowSize.y - height
                    if (param.x + width > windowSize.x)
                        param.x = windowSize.x - width
                    try {
                        windowManager?.updateViewLayout(overlayView, param)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun setGPSState(newLevel: WarningsBasicLevels) {
        widget?.setGPSState(newLevel)
    }

    override fun setDataTransferState(newLevel: WarningsBasicLevels) {
        widget?.setDataTransferState(newLevel)
    }

    override fun setBatteryState(newLevel: WarningsBasicLevels) {
        widget?.setBatteryState(newLevel)
    }

    private fun setupViews(context: Context) {
        overlayView =
            LayoutInflater.from(context).inflate(R.layout.overlay_bubble, null)
        params = defaultWindowParams
        params?.let { param ->
            param.gravity = Gravity.TOP or Gravity.START
            windowManager?.addView(overlayView, param)
            try {
                addOnTouchListener(param)
            } catch (e: Exception) {
            }
        }
        overlayView?.findViewById<View>(R.id.widget_closed)?.let {
            widgetClosedView = it
        }
        overlayView?.findViewById<View>(R.id.widget_open)?.let {
            widgetOpenView = it
        }
        overlayView?.findViewById<NkspoWidget>(R.id.widget)?.let { widget ->
            if (this.widget != null) return
            this.widget = widget
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addOnTouchListener(params: WindowManager.LayoutParams) {
        overlayView?.setOnTouchListener(
            object : View.OnTouchListener {
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                private var initialX = 0
                private var initialY = 0
                private var startTime = 0L
                private var touchShouldBeCalculated = false

                override fun onTouch(view: View?, motionevent: MotionEvent): Boolean {
                    when (motionevent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            touchShouldBeCalculated = false
                            // measure if it was clicked where it should
                            if (widgetOpenView?.visibility == View.GONE) {
                                // if we're closed, check if click was in closed area - otherwise do nothing
                                widgetClosedView?.let {
                                    it.measure(
                                        View.MeasureSpec.UNSPECIFIED,
                                        View.MeasureSpec.UNSPECIFIED
                                    )
                                    if (motionevent.x < it.x || motionevent.x > it.x + it.measuredWidth
                                        || motionevent.y < it.y || motionevent.y > it.y + it.measuredHeight
                                    ) {
                                        // if click was outside icon, do nothing
                                        return false
                                    }
                                }
                            }
                            params.alpha = 1.0f
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = motionevent.rawX
                            initialTouchY = motionevent.rawY
                            startTime = System.currentTimeMillis()
                            touchShouldBeCalculated = true

                        }
                        MotionEvent.ACTION_UP -> {
                            if (!touchShouldBeCalculated) return false
                            if (System.currentTimeMillis() - startTime < 500) {
                                // click
                                if (widgetOpenView?.visibility == View.VISIBLE) {
                                    // check if click was in bottom 70% of height - if yes, we assume it was request to open app
                                    overlayView?.let { overlay ->
                                        overlay.measure(
                                            View.MeasureSpec.UNSPECIFIED,
                                            View.MeasureSpec.UNSPECIFIED
                                        )
                                        val height = overlay.measuredHeight
                                        if (motionevent.rawY - initialY > height * 0.30) {
                                            // click
                                            startIntent?.let { intent ->
                                                applicationContext?.let { context ->
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    ContextCompat.startActivity(
                                                        context.applicationContext,
                                                        intent,
                                                        null
                                                    )
                                                }
                                            }
                                        } else {
                                            widgetOpenView?.visibility = View.GONE
                                        }
                                    }
                                } else {
                                    openOverlay()
                                }
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (!touchShouldBeCalculated) return false
                            overlayView?.let { overlay ->
                                overlayView?.measure(
                                    View.MeasureSpec.UNSPECIFIED,
                                    View.MeasureSpec.UNSPECIFIED
                                )
                                widgetClosedView?.measure(
                                    View.MeasureSpec.UNSPECIFIED,
                                    View.MeasureSpec.UNSPECIFIED
                                )
                                val width = overlay.measuredWidth
                                // for height we use small widget positions&size if it's closed
                                val height: Int =
                                    when {
                                        widgetOpenView?.visibility == View.VISIBLE -> overlay.measuredHeight
                                        widgetClosedView?.visibility == View.VISIBLE -> widgetClosedView?.measuredHeight
                                            ?: 0
                                        // fallback, should never happen
                                        else -> 100
                                    }
                                val movementDiffX = initialTouchX - motionevent.rawX
                                val movementDiffY = initialTouchY - motionevent.rawY
                                params.x = initialX - movementDiffX.toInt()
                                params.y = initialY - movementDiffY.toInt()
                                // check if we are in minX/minY rectangle
                                if (params.x < 0) params.x = 0
                                if (params.y < 0) params.y = 0
                                if (params.x + width > windowSize.x) params.x =
                                    windowSize.x - width

                                if (params.y + height > windowSize.y) params.y =
                                    windowSize.y - height
                                try {
                                    windowManager?.updateViewLayout(overlayView, params)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        else -> {
                        }
                    }
                    return false
                }
            })
    }


    private fun dpToPixels(dpSize: Int): Int {
        applicationContext?.resources?.displayMetrics?.let { displayMetrics ->
            return (dpSize * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toFloat()
                .roundToInt()
        }
        return 0
    }

    private fun setupServices(context: Context) {
        windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        inflater = context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager?.defaultDisplay?.getSize(windowSize)
    }

    private fun clearSystem() {
        widget = null
        widgetClosedView = null
        widgetOpenView = null
        if (windowManager == null) {
            return
        }
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
        windowManager = null
        inflater = null
    }
}
package pl.gov.mf.mobile.ui.components.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.res.ResourcesCompat
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.views.animated.AnimatedMfLogo
import pl.gov.mf.mobile.ui.components.views.animated.AnimationViewThread
import pl.gov.mf.mobile.ui.components.views.animated.AnimationViewThreadRxImplementation

class AnimatedMfLogoView(context: Context?, attrs: AttributeSet?) :
    SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var fgColor: Int = Color.RED
    private var bgColor: Int = Color.WHITE

    init {
        holder.addCallback(this)
        attrs?.let {
            val styleAttributes =
                context?.obtainStyledAttributes(it, R.styleable.AnimatedMfLogoView)
            bgColor =
                styleAttributes?.getInt(R.styleable.AnimatedMfLogoView_logo_background, Color.WHITE)
                    ?: Color.WHITE
            Log.d("BG_COLOR", "$bgColor")
        }
    }

    var logo: AnimatedMfLogo =
        AnimatedMfLogo(ResourcesCompat.getColor(resources, R.color.bloodRed, null))
    var animationThread: AnimationViewThread = AnimationViewThreadRxImplementation(listOf(logo))

    override fun surfaceCreated(holder: SurfaceHolder) {
        animationThread.start(holder)
        setBackgroundRes(bgColor)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        animationThread.stop()
        holder.surface.release()
    }

    private fun setBackgroundRes(color: Int) {
        animationThread.changeDefaultBackgroundColor(color)
    }

    private fun setForegroundRes(color: Int) {
        logo.changeDefaultForegroundColor(color)
    }
}


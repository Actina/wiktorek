package pl.gov.mf.mobile.ui.components.views.animated

import android.view.SurfaceHolder

interface AnimationViewThread {
    fun start(holder: SurfaceHolder)
    fun stop()
    fun changeDefaultBackgroundColor(color: Int)
}
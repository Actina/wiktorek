package pl.gov.mf.mobile.ui.components.views.animated

import android.graphics.Canvas

interface AnimatedElement {
    fun initialize(canvasWidth: Int, canvasHeight: Int)
    fun animate(timePassedInMsecs: Long)
    fun draw(canvas: Canvas)
}
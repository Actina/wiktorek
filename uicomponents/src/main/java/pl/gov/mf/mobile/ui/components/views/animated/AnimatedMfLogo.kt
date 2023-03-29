package pl.gov.mf.mobile.ui.components.views.animated

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class AnimatedMfLogo(private val foregroundColorRes: Int) : AnimatedElement {

    companion object {
        private const val fullCircleInSecs = 2
        private const val baseAngleSize = 120.0f
    }


    private var size = 24.0f
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    private fun strokeSize() = size * 5.0f / 6.0f

    private var paintBg: Paint = Paint().apply {
        color = Color.parseColor("#BABABA")
        strokeWidth = strokeSize()
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var paintFg: Paint = Paint().apply {
        color =
            if (foregroundColorRes != -1) foregroundColorRes else Color.RED
        strokeWidth = strokeSize()
        style = Paint.Style.STROKE
        isAntiAlias = true
    }


    override fun initialize(canvasWidth: Int, canvasHeight: Int) {
        this.canvasHeight = canvasHeight
        this.canvasWidth = canvasWidth
        size = minOf(canvasHeight, canvasWidth).toFloat() / 6.0f
        paintFg.strokeWidth = strokeSize()
        paintBg.strokeWidth = strokeSize()
    }

    var currentStart = 0.0f

    override fun animate(timePassedInMsecs: Long) {
        currentStart += (360.0f / fullCircleInSecs * timePassedInMsecs / 1000.0f)
        while (currentStart > 360.0f) currentStart -= 360.0f
    }

    override fun draw(canvas: Canvas) {
        drawAnimatedArc(canvas, 0.0f, canvas.width / 2.0f - size, canvas.height / 2.0f + size)
        drawAnimatedArc(canvas, -120.0f, canvas.width / 2.0f + size, canvas.height / 2.0f + size)
        drawAnimatedArc(canvas, 120.0f, canvas.width / 2.0f, canvas.height / 2.0f - size)
    }

    private fun drawAnimatedArc(
        canvas: Canvas,
        angleOffset: Float,
        centerX: Float,
        centerY: Float
    ) {
        canvas.drawArc(
            centerX - size,
            centerY - size,
            centerX + size,
            centerY + size,
            0.0f + angleOffset,
            240.0f,
            false,
            paintBg
        )
        if (currentStart < 240.0f) {
            var angle = baseAngleSize
            if (angle + currentStart > 240.0f)
                angle = baseAngleSize - (baseAngleSize - (240.0f - currentStart))
            canvas.drawArc(
                centerX - size,
                centerY - size,
                centerX + size,
                centerY + size,
                currentStart + angleOffset,
                angle,
                false,
                paintFg
            )
        } else if (currentStart > 360.0f - baseAngleSize) {
            canvas.drawArc(
                centerX - size,
                centerY - size,
                centerX + size,
                centerY + size,
                0.0f + angleOffset,
                currentStart + baseAngleSize - 360.0f,
                false,
                paintFg
            )
        }
    }

    fun changeDefaultForegroundColor(color: Int) {
        paintFg.color = color
    }

}
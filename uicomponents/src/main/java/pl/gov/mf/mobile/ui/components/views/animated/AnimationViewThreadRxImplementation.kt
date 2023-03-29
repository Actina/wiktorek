package pl.gov.mf.mobile.ui.components.views.animated

import android.graphics.Color
import android.view.SurfaceHolder
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pl.gov.mf.mobile.utils.disposeSafe
import java.util.concurrent.TimeUnit

class AnimationViewThreadRxImplementation(private val animatedElements: List<AnimatedElement>) :
    AnimationViewThread {

    companion object {
        const val FPS = 30L
    }

    private var backgroundColor: Int = Color.WHITE

    private var threadDisposable: Disposable? = null

    override fun start(holder: SurfaceHolder) {

        var lastTimestamp = System.currentTimeMillis()
        var initialized = false
        threadDisposable?.dispose()
        threadDisposable = Observable.interval(1000 / FPS, 1000 / FPS, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io()).subscribe({
                try {
                    val canvas = holder.lockCanvas()
                    canvas?.let {
                        if (!initialized) {
                            initialized = true
                            animatedElements.forEach {
                                it.initialize(
                                    canvasWidth = canvas.width,
                                    canvasHeight = canvas.height
                                )
                            }
                        }
                        val timeDiff = System.currentTimeMillis() - lastTimestamp
                        lastTimestamp = System.currentTimeMillis()
                        // now animate
                        animatedElements.forEach { it.animate(timeDiff) }
                        // and draw
                        canvas.drawColor(backgroundColor)
                        animatedElements.forEach { it.draw(canvas) }
                        holder.unlockCanvasAndPost(canvas)
                    }
                } catch (ex: Exception) {
                    // do nothing
                    threadDisposable.disposeSafe()
                }
            }, {
                stop()
            })
    }

    override fun stop() {
        threadDisposable.disposeSafe()
        threadDisposable = null
    }

    override fun changeDefaultBackgroundColor(color: Int) {
        backgroundColor = color
    }
}
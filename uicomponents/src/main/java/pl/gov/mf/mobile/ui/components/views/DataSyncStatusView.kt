package pl.gov.mf.mobile.ui.components.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.LottieAnimationView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.databinding.ViewSyncStatusBinding
import pl.gov.mf.mobile.ui.components.utils.match
import pl.gov.mf.mobile.utils.disposeSafe
import pl.gov.mf.mobile.utils.translate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


@SuppressLint("SetTextI18n")
class DataSyncStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val disappearAnimation: Animation
    private val appearAnimation: Animation
    private var status: DataSyncStatus = DataSyncStatus.HIDDEN

    private lateinit var syncContentStatusReady: TextView
    private lateinit var syncContentStatusLoading: TextView
    private lateinit var syncImageStatusReady: ImageView
    private lateinit var syncImageStatusLoading: LottieAnimationView

    private val binding: ViewSyncStatusBinding =
        DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_sync_status,
            this,
            false
        )

    override fun invalidate() {
        super.invalidate()
        binding.invalidateAll()
    }

    init {
        val set = ConstraintSet()
        setupView(binding.root)
        addView(binding.syncRoot)
        set.clone(this)
        set.match(binding.syncRoot, this)
        syncContentStatusReady.text = "dashboard_empty_sync_finished".translate(context)
        syncContentStatusLoading.text = "dashboard_empty_sync_in_progress".translate(context)
        setOnClickListener {
            // do nothing, just invalidate touch
        }
        disappearAnimation = AnimationUtils.loadAnimation(context, R.anim.disappear_anim)
        appearAnimation = AnimationUtils.loadAnimation(context, R.anim.appear_anim)
    }

    private fun setupView(view: View) {
        syncContentStatusReady = view.findViewById(R.id.sync_content_status_ready)
        syncContentStatusLoading = view.findViewById(R.id.sync_content_status_loading)
        syncImageStatusReady = view.findViewById(R.id.sync_image_status_ready)
        syncImageStatusLoading = view.findViewById(R.id.sync_image_status_loading)
    }

    private val hideCounter = AtomicInteger(-1)
    private var counterDisposable: Disposable? = null

    fun onStop() {
        // stop thread
        counterDisposable.disposeSafe()
    }

    fun onStart() {
        hideCounter.set(-1)
        invalidateView(true)
        // start thread
        counterDisposable.disposeSafe()
        counterDisposable = Observable.interval(1, TimeUnit.SECONDS).subscribe {
            if (status == DataSyncStatus.FINISHED)
                if (hideCounter.decrementAndGet() <= 0) {
                    status = DataSyncStatus.HIDDEN
                    hideCounter.set(-1)
                    invalidateView()
                }
        }
    }

    fun startSync() {
        if (status != DataSyncStatus.IN_PROGRESS)
            status = DataSyncStatus.IN_PROGRESS
        invalidateView()
    }

    fun finishSync() {
        if (status != DataSyncStatus.HIDDEN)
            status = DataSyncStatus.FINISHED
        invalidateView()
    }

    private fun invalidateView(initial: Boolean = false) {
        post {
            Log.d("ANIM_STAT", "$status")
            // this is done by other func, left here to not forget in case of refactor/fixes
            // hideCounter.set(-1)
            when (status) {
                DataSyncStatus.HIDDEN -> {
                    if (visibility != GONE) {
                        if (!initial) {
                            disappearAnimation.setAnimationListener(object :
                                Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {
                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                    if (counterDisposable?.isDisposed == false)
                                        post {
                                            visibility = GONE
                                        }
                                }

                                override fun onAnimationRepeat(animation: Animation?) {
                                }

                            })
                            startAnimation(disappearAnimation)
                        } else {
                            visibility = GONE
                        }
                    }
                }
                DataSyncStatus.FINISHED -> {
                    visibility = VISIBLE
                    alpha = 1.0f
                    // always start from same amount
                    hideCounter.set(TIME_BEFORE_DISAPPEAR)
                    syncImageStatusReady.visibility = VISIBLE
                    syncImageStatusLoading.visibility = INVISIBLE
                    syncContentStatusReady.visibility = VISIBLE
                    syncContentStatusLoading.visibility = INVISIBLE
                }
                DataSyncStatus.IN_PROGRESS -> {
                    if (visibility != VISIBLE) {
                        visibility = VISIBLE
                        startAnimation(appearAnimation)
                    } else {
                        alpha = 1.0f
                    }
                    syncImageStatusReady.visibility = INVISIBLE
                    syncImageStatusLoading.visibility = VISIBLE
                    syncContentStatusReady.visibility = INVISIBLE
                    syncContentStatusLoading.visibility = VISIBLE
                }
            }
        }
    }

    fun resetState() {
        status = DataSyncStatus.HIDDEN
        hideCounter.set(-1)
    }

    enum class DataSyncStatus {
        HIDDEN, FINISHED, IN_PROGRESS
    }

    companion object {
        private const val TIME_BEFORE_DISAPPEAR = 5
    }
}
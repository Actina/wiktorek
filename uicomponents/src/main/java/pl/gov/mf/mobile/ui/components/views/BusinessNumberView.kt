package pl.gov.mf.mobile.ui.components.views

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.databinding.ViewBusinessNumberBinding
import pl.gov.mf.mobile.ui.components.utils.match
import pl.gov.mf.mobile.utils.PredefiniedIntents
import pl.gov.mf.mobile.utils.translate

class BusinessNumberView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: ViewBusinessNumberBinding = ViewBusinessNumberBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    private lateinit var viewBnBusinessNumber: TextView
    private lateinit var viewBnBusinessNumberHeader: TextView
    private lateinit var viewBnBusinessNumberIcon: ImageView

    init {
        //.inflate(R.layout.view_business_number, this, false)
        val set = ConstraintSet()
        setupView(binding.root)
        addView(binding.viewBn)
        set.clone(this)
        set.match(binding.viewBn, this)
    }

    private fun setupView(view: View) {
        viewBnBusinessNumber = view.findViewById(R.id.view_bn_business_number)
        viewBnBusinessNumberHeader = view.findViewById(R.id.view_bn_business_number_header)
        viewBnBusinessNumberIcon = view.findViewById(R.id.view_bn_business_number_icon)
    }

    private var lastClickTimestamp = 0L
    private val minTimeBetweenClicks = 3000L

    override fun invalidate() {
        super.invalidate()
        binding.invalidateAll()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // business number
        val businessNumber =
            (context.applicationContext as BaseApplication).getApplicationComponent()
                .useCaseGetBusinessNumber().execute()
        viewBnBusinessNumber.text = businessNumber
        // actions
        setOnClickListener {
            if (System.currentTimeMillis() - lastClickTimestamp > minTimeBetweenClicks) {
                lastClickTimestamp = System.currentTimeMillis()
                context.startActivity(
                    Intent.createChooser(
                        PredefiniedIntents.shareIntent(businessNumber),
                        null
                    )
                )
            }
        }
    }

    private var lastCalculatedWidth = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (width <= 0 || width == lastCalculatedWidth) return

        lastCalculatedWidth = width
        val textLong = "toolbar_business_identifier_key".translate(context)
        val textShort = "toolbar_business_identifier_key_short".translate(context)
        val bounds = Rect()
        viewBnBusinessNumberHeader.paint.getTextBounds(
            textLong,
            0,
            textLong.length,
            bounds
        )
        val longTextLengthInPx = bounds.width()
        val layoutParams =
            viewBnBusinessNumberHeader.layoutParams as ConstraintLayout.LayoutParams
        val layoutParamsBn = viewBnBusinessNumber.layoutParams as ConstraintLayout.LayoutParams
        val layoutParamsIcon =
            viewBnBusinessNumberIcon.layoutParams as ConstraintLayout.LayoutParams
        val viewLengthInPixel =
            width - (viewBnBusinessNumber.width + layoutParams.leftMargin + layoutParams.rightMargin
                    + viewBnBusinessNumberIcon.width
                    + layoutParamsIcon.leftMargin + layoutParamsIcon.rightMargin
                    + layoutParamsBn.leftMargin + layoutParamsBn.rightMargin)
        if (viewLengthInPixel < longTextLengthInPx)
            viewBnBusinessNumberHeader.text = textShort
        else
            viewBnBusinessNumberHeader.text = textLong
    }
}
package pl.gov.mf.mobile.ui.components.views

import android.content.Context
import android.media.Image
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.interfaces.DataSenderStateInterface
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.mobile.ui.components.utils.match
import pl.gov.mf.mobile.utils.toColorInMode
import pl.gov.mf.mobile.utils.translate

class DashboardStatusIconInternet @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), DataSenderStateInterface {

    private lateinit var dashboardIconDrawable: ImageView
    private lateinit var dashboardIconStatusBar: ImageView
    private lateinit var dashboardIconText: TextView
    private lateinit var dashboardIconRoot: ConstraintLayout

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_dashboard_icon, this, false)
        val set = ConstraintSet()
        setupView(view)
        addView(view)
        set.clone(this)
        set.match(view, this)

        // customize view
        dashboardIconDrawable.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_network_communication_light,
                null
            )
        )
        dashboardIconText.text = "dashboard_indicators_internet".translate(context)
        dashboardIconStatusBar.background =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_dashboard_status_gray_light,
                null
            )
        invalidateBindings()
    }

    private fun setupView(view: View) {
        dashboardIconDrawable = view.findViewById(R.id.dashboard_icon_drawable)
        dashboardIconStatusBar = view.findViewById(R.id.dashboard_icon_statusbar)
        dashboardIconText = view.findViewById(R.id.dashboard_icon_text)
        dashboardIconRoot = view.findViewById(R.id.dashboard_icon_root)
    }

    fun invalidateBindings() {
        dashboardIconRoot.apply {
            background = getBackgroundInMode()
        }
        dashboardIconText.setTextColor("textPrimary".toColorInMode(context))
        handler?.post { updateVisuals() }
    }

    private fun getBackgroundInMode() =
        when ((context.applicationContext as BaseApplication).getApplicationComponent()
            .useCaseGetCurrentAppMode()
            .execute()
        ) {
            AppMode.DARK_MODE -> ContextCompat.getDrawable(
                context,
                android.R.drawable.dialog_holo_dark_frame
            )
            AppMode.LIGHT_MODE -> ContextCompat.getDrawable(
                context,
                android.R.drawable.dialog_holo_light_frame
            )
        }

    private var currentLevel = WarningsBasicLevels.UNKNOWN

    override fun onDataSenderStateChanged(newLevel: WarningsBasicLevels) {
        if (currentLevel == newLevel) return
        currentLevel = newLevel
        handler?.post { updateVisuals() }
    }

    private fun updateVisuals() {
        val darkMode = (context.applicationContext as BaseApplication).getApplicationComponent()
            .useCaseGetCurrentAppMode()
            .execute() != AppMode.LIGHT_MODE
        val setup = when (currentLevel) {
            WarningsBasicLevels.GREEN -> {
                if (darkMode)
                    Pair(
                        R.drawable.ic_dashboard_status_green_dark,
                        R.drawable.ic_network_communication_green_dark
                    )
                else
                    Pair(
                        R.drawable.ic_dashboard_status_green_light,
                        R.drawable.ic_network_communication_green_light
                    )
            }
            WarningsBasicLevels.YELLOW -> {
                if (darkMode)
                    Pair(
                        R.drawable.ic_dashboard_status_yellow_dark,
                        R.drawable.ic_network_communication_yellow_dark
                    ) else
                    Pair(
                        R.drawable.ic_dashboard_status_yellow_light,
                        R.drawable.ic_network_communication_yellow_light
                    )
            }
            WarningsBasicLevels.RED -> {
                if (darkMode)
                    Pair(
                        R.drawable.ic_dashboard_status_red_dark,
                        R.drawable.ic_network_communication_red_dark
                    ) else
                    Pair(
                        R.drawable.ic_dashboard_status_red_light,
                        R.drawable.ic_network_communication_red_light
                    )
            }
            WarningsBasicLevels.UNKNOWN -> {
                if (darkMode)
                    Pair(
                        R.drawable.ic_dashboard_status_gray_dark,
                        R.drawable.ic_network_communication_dark
                    ) else
                    Pair(
                        R.drawable.ic_dashboard_status_gray_light,
                        R.drawable.ic_network_communication_light
                    )
            }
        }
        dashboardIconStatusBar.background =
            ResourcesCompat.getDrawable(
                resources,
                setup.first,
                null
            )
        dashboardIconDrawable.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                setup.second,
                null
            )
        )
    }
}

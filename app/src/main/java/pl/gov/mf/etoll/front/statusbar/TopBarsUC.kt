package pl.gov.mf.etoll.front.topBars

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import pl.gov.mf.etoll.R
import javax.inject.Inject

sealed class TopBarsUC {

    class SetTopBarsColorUseCase @Inject constructor() : TopBarsUC() {
        fun execute(
            activity: Activity, statusColor: Int = TopBars.STATUS_BAR.defaultColor,
        ) {
            if (activity is AppCompatActivity) {
                with(activity.window) {
                    statusBarColor = context.getColor(statusColor)
                }
            }
        }

    }
}

enum class TopBars(@ColorRes val defaultColor: Int) {
    //If you need to change default color, do it here
    STATUS_BAR(R.color.toolbarDark)
}

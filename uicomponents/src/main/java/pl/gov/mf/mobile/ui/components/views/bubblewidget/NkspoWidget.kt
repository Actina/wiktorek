package pl.gov.mf.mobile.ui.components.views.bubblewidget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels
import pl.gov.mf.etoll.ui.components.R

class NkspoWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_etoll, this)
    }

    private val gpsIcon = findViewById<ImageView>(R.id.gps)
    private val batteryIcon = findViewById<ImageView>(R.id.battery)
    private val dataIcon = findViewById<ImageView>(R.id.data)

    fun setBatteryState(state: WarningsBasicLevels) {
        GlobalScope.launch(Dispatchers.Main) {
            batteryIcon.imageTintList = getColor(state)
        }
    }

    fun setGPSState(state: WarningsBasicLevels) {
        GlobalScope.launch(Dispatchers.Main) {
            gpsIcon.imageTintList = getColor(state)
        }
    }

    fun setDataTransferState(state: WarningsBasicLevels) {
        GlobalScope.launch(Dispatchers.Main) {
            dataIcon.imageTintList = getColor(state)
        }
    }

    private fun getColor(state: WarningsBasicLevels) =
        when (state) {
            WarningsBasicLevels.GREEN -> getColor(R.color.ok)
            WarningsBasicLevels.YELLOW -> getColor(R.color.yellow)
            WarningsBasicLevels.RED -> getColor(R.color.not_ok)
            WarningsBasicLevels.UNKNOWN -> getColor(R.color.grayish)
        }

    private fun getColor(@ColorRes res: Int) =
        ResourceHelper.getColor(context, res)
}
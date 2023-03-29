package pl.gov.mf.etoll.front.onboarding.viewpager

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.gov.mf.etoll.R
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setSrc
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setTextColorInMode
import pl.gov.mf.mobile.utils.setTranslatedText

class OnboardingViewPagerAdapter : RecyclerView.Adapter<PagerVH>() {

    lateinit var pages: List<OnboardingPage>
    private var invalidateBindings: Boolean = false


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PagerVH = PagerVH(
        LayoutInflater.from(parent.context).inflate(R.layout.row_onboarding_page, parent, false)
    )

    override fun getItemCount(): Int = pages.size

    override fun onBindViewHolder(
        holder: PagerVH,
        position: Int,
    ) = holder.itemView.run {
        findViewById<TextView>(R.id.row_onboarding_viewpager_title).setTranslatedText(pages[position].titleRes)
        findViewById<TextView>(R.id.row_onboarding_viewpager_content).setTranslatedText(pages[position].textRes)

        if (invalidateBindings) {
            findViewById<ImageView>(R.id.row_onboarding_viewpager_image).setSrc(pages[position].imageName)
            findViewById<TextView>(R.id.row_onboarding_viewpager_title).setTextColorInMode("spoeHeader")
            findViewById<TextView>(R.id.row_onboarding_viewpager_content).setTextColorInMode("spoeOnboarding")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyBindingsChanged() {
        // TODO: refactor - dark mode - include mode names inside enum instead of manually "rebinding"
        invalidateBindings = true
        notifyDataSetChanged()
    }
}

class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView)
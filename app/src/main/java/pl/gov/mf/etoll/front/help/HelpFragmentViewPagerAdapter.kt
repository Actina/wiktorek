package pl.gov.mf.etoll.front.help

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import pl.gov.mf.etoll.databinding.RowHelpSentBinding
import pl.gov.mf.etoll.databinding.RowHelpTolledBinding

class HelpFragmentViewPagerAdapter : RecyclerView.Adapter<HelpFragmentViewPagerViewHolder>() {

    var callbacks: HelpAdapterCallbacks? = null
    private var pages = 0
    private lateinit var pagesConfiguration: HelpViewModel.HelpConfiguration
    private var invalidateBindings: Boolean = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HelpFragmentViewPagerViewHolder =
        HelpFragmentViewPagerViewHolder(
            if (viewType == 0)
                RowHelpTolledBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            else
                RowHelpSentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
        )

    override fun getItemCount(): Int = pages

    override fun getItemViewType(position: Int): Int =
        if (pagesConfiguration == HelpViewModel.HelpConfiguration.BOTH) position else if (pagesConfiguration == HelpViewModel.HelpConfiguration.SENT) 1 else 0

    override fun onBindViewHolder(
        holder: HelpFragmentViewPagerViewHolder,
        position: Int,
    ) {
        holder.tutorialButton?.setOnClickListener {
            callbacks?.onShowTutorialRequested(if (pagesConfiguration == HelpViewModel.HelpConfiguration.BOTH) position == 1 else pagesConfiguration == HelpViewModel.HelpConfiguration.SENT)
        }
        holder.websiteButton?.setOnClickListener {
            if (it is TextView)
                callbacks?.onShowWebsiteRequested(it.text.toString())
        }
        holder.emailButton?.setOnClickListener {
            if (it is TextView)
                callbacks?.onSendEmailRequested(it.text.toString())
        }
        holder.phoneButton?.setOnClickListener {
            if (it is TextView)
                callbacks?.onCallPhoneRequested(it.text.toString())
        }
        invalidateBindings(holder, position)
    }

    private fun invalidateBindings(
        holder: HelpFragmentViewPagerViewHolder,
        position: Int,
    ) {
        if (invalidateBindings) {
            holder.binding.invalidateAll()
            if (position == itemCount - 1) {
                invalidateBindings = false
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyBindingsChanged() {
        invalidateBindings = true
        notifyDataSetChanged()
    }

    fun initializePages(configuration: HelpViewModel.HelpConfiguration) {
        pages = when (configuration) {
            HelpViewModel.HelpConfiguration.TOLLED -> 1
            HelpViewModel.HelpConfiguration.SENT -> 1
            HelpViewModel.HelpConfiguration.BOTH -> 2
        }
        pagesConfiguration = configuration
    }

}

interface HelpAdapterCallbacks {
    fun onCallPhoneRequested(number: String)
    fun onShowTutorialRequested(sent: Boolean)
    fun onSendEmailRequested(email: String)
    fun onShowWebsiteRequested(website: String)
}

class HelpFragmentViewPagerViewHolder(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {
    var tutorialButton: Button? = null
    var phoneButton: View? = null
    var emailButton: View? = null
    var websiteButton: View? = null

    init {
        if (binding is RowHelpTolledBinding) {
            tutorialButton = binding.helpTutorialSeeButton
            phoneButton = binding.helpPhone
            emailButton = binding.helpMail
            websiteButton = binding.helpWebsite
        } else if (binding is RowHelpSentBinding) {
            tutorialButton = binding.helpTutorialSeeButton
            phoneButton = binding.helpPhone
            emailButton = binding.helpMail
            websiteButton = binding.helpWebsite
        }
    }
}
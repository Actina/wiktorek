package pl.gov.mf.etoll.front.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.CardSentDetailsBinding

class CardSentSingleDetails(model: CardSentSingleDetailsViewModel) :
    BaseCard<CardSentSingleDetailsViewModel>(model) {

    private var callbacks: CardSentDetailsCallbacks? = null

    override fun bind(
        context: Context,
        root: ViewGroup
    ): View {
        val binding: CardSentDetailsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.card_sent_details,
            root,
            false
        )
        binding.root.setOnClickListener {
            callbacks?.onSentDetailsRequested(
                viewModel.sentNumber.value ?: ""
            )
        }
        binding.item = viewModel
        return binding.root
    }

    fun setCallbacks(callback: CardSentDetailsCallbacks?) {
        this.callbacks = callback
    }
}

data class CardSentSingleDetailsViewModel(
    val sentNumber: LiveData<String>,
    var sentDates: LiveData<String>
)

interface CardSentDetailsCallbacks {
    fun onSentDetailsRequested(sentNumber: String)
}
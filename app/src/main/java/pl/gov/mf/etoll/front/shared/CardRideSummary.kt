package pl.gov.mf.etoll.front.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.CardRideSummaryBinding

class CardRideSummary(model: CardRideSummaryViewModel) : BaseCard<CardRideSummaryViewModel>(model) {

    override fun bind(context: Context, root: ViewGroup): View {
        val binding: CardRideSummaryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.card_ride_summary,
            root,
            false
        )
        binding.item = viewModel
        return binding.root
    }
}

data class CardRideSummaryViewModel(
    val rideLength: LiveData<String>,
    var rideStart: LiveData<String>,
    var rideEnd: LiveData<String>
)
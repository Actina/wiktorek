package pl.gov.mf.etoll.front.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.CardTolledSummaryBinding

class CardSummaryTolled(model: CardSummaryTolledViewModel) :
    BaseCard<CardSummaryTolledViewModel>(model) {

    override fun bind(context: Context, root: ViewGroup): View {
        val binding: CardTolledSummaryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.card_tolled_summary,
            root,
            false
        )
        binding.item = viewModel
        return binding.root
    }
}

data class CardSummaryTolledViewModel(
    val vehicleBrand: LiveData<String>,
    val vehicleModel: LiveData<String>,
    val vehicleLicensePlate: LiveData<String>,
    val vehicleCategory: LiveData<String>,
    val vehicleEmissionClass: LiveData<String>,
)
package pl.gov.mf.etoll.front.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.CardDeviceBinding

class CardDevice(model: CardDeviceViewModel) : BaseCard<CardDeviceViewModel>(model) {

    override fun bind(context: Context, root: ViewGroup): View {
        val binding: CardDeviceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.card_device,
            root,
            false
        )
        binding.item = viewModel
        return binding.root
    }
}

data class CardDeviceViewModel(
    val trackingDeviceName: LiveData<String>
)
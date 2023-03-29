package pl.gov.mf.etoll.front.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.CardSyncStatusBinding

class CardDataSyncStatus(model: CardDataSyncStatusViewModel) :
    BaseCard<CardDataSyncStatusViewModel>(model) {
    var binding: CardSyncStatusBinding? = null

    override fun bind(context: Context, root: ViewGroup): View {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.card_sync_status,
            root,
            false
        )
        binding!!.item = viewModel
        return binding!!.root
    }

    override fun refresh() {
        binding?.invalidateAll()
    }
}


data class CardDataSyncStatusViewModel(val isLoading: MutableLiveData<Boolean>)
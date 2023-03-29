package pl.gov.mf.etoll.front.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.CardSentHeaderBinding

class CardSentHeader(model: CardSentHeaderViewModel) : BaseCard<CardSentHeaderViewModel>(model) {

    override fun bind(context: Context, root: ViewGroup): View {
        val binding: CardSentHeaderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.card_sent_header,
            root,
            false
        )
        binding.item = viewModel
        return binding.root
    }
}

data class CardSentHeaderViewModel(private val dummy: Int = 0)
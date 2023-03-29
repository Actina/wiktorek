package pl.gov.mf.etoll.front.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.model.CoreAccountTypes
import pl.gov.mf.etoll.databinding.CardAccountBinding

class CardAccount(model: CardAccountViewModel) : BaseCard<CardAccountViewModel>(model) {

    override fun bind(context: Context, root: ViewGroup): View {
        val binding: CardAccountBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.card_account,
            root,
            false
        )
        binding.item = viewModel
        return binding.root
    }
}

data class CardAccountViewModel(
    val accountAlias: LiveData<String>,
    var accountId: LiveData<String>,
    var accountType: CoreAccountTypes
) {
    val untranslatedAccountType: LiveData<String> = MutableLiveData(accountType.uiLiteral)
}
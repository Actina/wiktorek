package pl.gov.mf.etoll.utils

import pl.gov.mf.etoll.commons.CurrencyUtils.CURRENCY_STRING
import pl.gov.mf.etoll.commons.CurrencyUtils.formatCash
import pl.gov.mf.etoll.core.model.CoreAccountTypes
import pl.gov.mf.etoll.core.model.CoreAccountTypes.POSTPAID
import pl.gov.mf.mobile.ui.components.adapters.TranslationsAdapter
import pl.gov.mf.mobile.ui.components.adapters.TranslationsAdapter.toConditionallyTranslatedText

fun formatAccountBalanceText(
    balanceValue: Double?,
    accountInitialized: Boolean,
    accountType: String?
): TranslationsAdapter.ConditionallyTranslatedText =
    when {
        accountInitialized && balanceValue!=null -> "${balanceValue.formatCash()} $CURRENCY_STRING".toConditionallyTranslatedText()
        POSTPAID == CoreAccountTypes.fromLiteral(accountType) ->
            POSTPAID.uiLiteral.toConditionallyTranslatedText(shouldBeTranslated = true)
        else -> "-".toConditionallyTranslatedText()
    }
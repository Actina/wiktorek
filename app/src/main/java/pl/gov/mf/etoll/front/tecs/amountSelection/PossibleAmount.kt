package pl.gov.mf.etoll.front.tecs.amountSelection

import pl.gov.mf.etoll.commons.CurrencyUtils

data class PossibleAmount(
    val amount: String,
    val currency: String = " " + CurrencyUtils.CURRENCY_STRING
)

fun PossibleAmount.withCurrency() = amount + currency
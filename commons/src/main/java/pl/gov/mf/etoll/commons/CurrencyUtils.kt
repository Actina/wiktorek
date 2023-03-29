package pl.gov.mf.etoll.commons

import android.text.InputFilter
import android.text.Spanned
import pl.gov.mf.etoll.commons.CurrencyUtils.formatCash


object CurrencyUtils {

    const val CURRENCY_STRING = "PLN"

    // warning, this part is used commonly in api communication! It's not related directly to cash stuff
    fun Double.format(digits: Int) = "%.${digits}f".format(this).replace(",", ".")

    // this formatter is used for cash showing purposes in ui
    fun Double.formatCash() = "%.${2}f".format(this).replace(".", ",")

    fun Double.formatAccountValue(): String = "%.2f".format(this).formatAccountValue()

    fun String.formatAccountValue(): String = replace(".", ",") + " " + CURRENCY_STRING

    fun String.toStringAmount(): String = replace(",", ".").toDouble().formatAccountValue()

    fun String.toAmount(): Double = substringBefore(CURRENCY_STRING).replace(",", ".").toDouble()

    class CurrencyInputFilter :
        InputFilter {
        private val digitsAfterZero = 2
        private val amountOfDigits = 7

        private fun isMatch(text: String): Boolean {
            // avoid double zeroes at start
            if (text.length > 1 && text[0] == '0' && text[1] != ',') return false
            // if there are two sections, make sure second one has max of 2 elements and first is not too long
            if (text.contains(",")) {
                return text.split(",").size == 2 && text.split(",")[0].length < amountOfDigits - digitsAfterZero && text.split(
                    ","
                )[0].isNotEmpty() && text.split(
                    ","
                )[1].length <= digitsAfterZero
            }
            // just check total length
            return text.length < amountOfDigits - digitsAfterZero
        }

        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            //We cannot make any changes to dest! (so we make copies - also for other strings)
            val destCopy = String(dest.toString().toCharArray())
            val sourceCopy = String(source.toString().toCharArray())
            val newTextInInput = destCopy.replaceRange(dstart, dend, sourceCopy)

            return if (isMatch(newTextInInput)) null else ""
        }

    }
}
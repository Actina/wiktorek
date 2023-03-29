package pl.gov.mf.mobile.utils

import android.content.Intent
import android.net.Uri

class PredefiniedIntents {
    companion object {
        fun shareIntent(text: String): Intent =
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }

        fun helpEmailIntent(targetMail: String): Intent =
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$targetMail")
            }

        fun openWebsiteIntent(url: String): Intent =
            Intent(Intent.ACTION_VIEW).apply {
                val httpPrefix = "http://"
                val httpsPrefix = "https://"
                val validUrl =
                    if (!url.startsWith(httpPrefix) && !url.startsWith(httpsPrefix))
                        httpsPrefix + url else url
                data = Uri.parse(validUrl)
            }

        fun callPhoneIntent(phoneNumber: String): Intent =
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }

    }
}
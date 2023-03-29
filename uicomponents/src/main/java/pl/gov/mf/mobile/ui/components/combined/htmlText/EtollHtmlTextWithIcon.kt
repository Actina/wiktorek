package pl.gov.mf.mobile.ui.components.combined.htmlText

import android.text.style.URLSpan
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.launch
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.compose.combined.text.EtollHtmlText
import pl.gov.mf.etoll.ui.components.compose.helpers.getIconInMode
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.h4bold


//TODO: Consider further refactor - replacing HtmlText with Text
@Composable
fun EtollHtmlTextWithIcon(
    htmlText: String,
    onLinkClicked: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val urlFromHtmlText = htmlText.getUrlFromText()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.S_SPACING)
            .padding(horizontal = Dimens.XL_SPACING)
            .clickable(enabled = true,
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = {
                    //Workaround to get url from text regardless of the HtmlText implementation
                    onLinkClicked(urlFromHtmlText)
                }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = getIconInMode(
                    icLightRes = R.drawable.ic_website_blue_light,
                    icDarkRes = R.drawable.ic_website_blue_dark
                )
            ),
            modifier = Modifier.padding(start = Dimens.S_SPACING),
            contentDescription = "",
        )
        val interactionScope = rememberCoroutineScope()
        EtollHtmlText(
            text = htmlText,
            textStyle = EtollTheme.typography.h4bold,
            modifier = Modifier
                .padding(start = Dimens.L_SPACING)
                .clickable(enabled = true,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        //Do nothing
                    })
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            val pressInteraction = PressInteraction.Press(Offset.Zero)
                            try {
                                interactionScope.launch {
                                    interactionSource.emit(pressInteraction)
                                }
                                awaitRelease()
                            } finally {
                                interactionScope.launch {
                                    interactionSource.emit(PressInteraction.Release(pressInteraction))
                                }
                            }
                        },
                        onTap = {
                            onLinkClicked(urlFromHtmlText)
                        })
                },
        )
    }
}

/* Compare this function with: private fun String.asHTML(...) from HtmlText
 - we get url from text similar way */
private fun String.getUrlFromText(): String = buildAnnotatedString {
    val spanned = HtmlCompat.fromHtml(this@getUrlFromText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    val spans = spanned.getSpans(0, spanned.length, Any::class.java)
    append(spanned.toString())

    spans.forEach { span ->
        val start = spanned.getSpanStart(span)
        val end = spanned.getSpanEnd(span)
        if (span is URLSpan)
            addStringAnnotation(
                tag = "url_tag",
                annotation = span.url,
                start = start,
                end = end
            )
    }
}.text

@Composable
@Preview
fun EtollHtmlTextWithIcon() {
    EtollHtmlTextWithIcon(
        htmlText = "<![CDATA[<a href=\"https://www.mojekonto.etoll.gov.pl\">www.mojekonto.etoll.gov.pl</a>]]>",
        onLinkClicked = {})
}
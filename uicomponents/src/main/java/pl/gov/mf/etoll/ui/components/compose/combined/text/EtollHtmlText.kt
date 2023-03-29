package pl.gov.mf.etoll.ui.components.compose.combined.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.ireward.htmlcompose.HtmlText
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.p2BoldLink
import pl.gov.mf.etoll.ui.components.compose.theme.p2regular

@Composable
fun EtollHtmlText(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = EtollTheme.typography.p2regular.copy(color = EtollTheme.colors.spoeTextContent),
) {
    HtmlText(
        text = text,
        style = textStyle,
        modifier = modifier,
        linkClicked = null,
        URLSpanStyle = SpanStyle(
            color = EtollTheme.colors.linkColor,
            textDecoration = TextDecoration.Underline,
            fontFamily = EtollTheme.typography.p2BoldLink.fontFamily,
            fontSize = EtollTheme.typography.p2BoldLink.fontSize,
            letterSpacing = EtollTheme.typography.p2BoldLink.letterSpacing,
        )
    )
}

@Composable
@Preview
fun EtollHtmlTextPreview() {
    EtollHtmlText(text = "<![CDATA[<a href=\"https://www.mojekonto.etoll.gov.pl\">www.mojekonto.etoll.gov.pl</a>]]>")
}
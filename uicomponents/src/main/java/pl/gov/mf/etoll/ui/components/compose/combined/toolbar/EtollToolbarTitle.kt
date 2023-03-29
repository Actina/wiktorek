package pl.gov.mf.etoll.ui.components.compose.combined.toolbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.toolbarDefault

//TODO: Note: toolbar text color in design is not always white in dark mode - style will be extending
@Composable
fun EtollToolbarTitle(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = TextAlign.Center,
        style = EtollTheme.typography.toolbarDefault,
        color = EtollTheme.colors.toolbarTextAlwaysWhite
    )
}

@Composable
fun EtollToolbarTitleDefault(modifier: Modifier = Modifier, rowScope: RowScope, text: String) {
    rowScope.run {
        EtollToolbarTitle(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f),
            text = text
        )
    }
}

@Preview
@Composable
fun EtollToolbarTitlePreview() {
    EtollToolbarTitle(text = "Some title")
}
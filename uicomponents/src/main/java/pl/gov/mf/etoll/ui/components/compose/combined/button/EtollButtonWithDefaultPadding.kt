package pl.gov.mf.etoll.ui.components.compose.combined.button

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens

@Composable
fun EtollButtonWithDefaultPadding(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    EtollButton(modifier = modifier.padding(Dimens.XL_SPACING), text = text) {
        onClick()
    }
}

@Preview
@Composable
fun EtollButtonWithDefaultPaddingPreview() {
    EtollButtonWithDefaultPadding(text = "Button") {
    }
}
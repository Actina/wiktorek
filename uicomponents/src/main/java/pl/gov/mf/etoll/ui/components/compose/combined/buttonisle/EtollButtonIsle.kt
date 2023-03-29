package pl.gov.mf.etoll.ui.components.compose.combined.buttonisle

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButtonWithDefaultPadding
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme

@Composable
fun EtollButtonIsle(
    modifier: Modifier = Modifier,
    button: @Composable () -> Unit,
) {
    Surface(modifier = modifier, color = EtollTheme.colors.buttonIsleBackground) {
        button()
    }
}

@Preview
@Composable
fun EtollButtonIslePreview() {
    EtollButtonIsle(button = {
        EtollButtonWithDefaultPadding(text = "Button") {

        }
    })
}
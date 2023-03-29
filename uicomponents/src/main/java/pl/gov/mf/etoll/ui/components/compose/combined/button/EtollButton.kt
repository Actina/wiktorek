package pl.gov.mf.etoll.ui.components.compose.combined.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.buttonDefault

@Composable
fun EtollButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit = {}) {
    Button(modifier = modifier
        .fillMaxWidth()
        .height(Dimens.BUTTON_HEIGHT),
        onClick = onClick) {
        Text(text = text, color = EtollTheme.colors.buttonText,
            style = EtollTheme.typography.buttonDefault)
    }
}


@Preview
@Composable
fun Sample() {
    EtollButton(text = "Button")
}
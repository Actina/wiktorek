package pl.gov.mf.etoll.ui.components.compose.combined.buttonisle

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.compose.combined.isledivider.EtollIsleDivider
import pl.gov.mf.etoll.ui.components.compose.combined.isledivider.IsleDividerShadeType
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButtonWithDefaultPadding

@Composable
fun EtollButtonIsleWithDivider(button: @Composable () -> Unit) {
    Column {
        EtollIsleDivider(dividerType = IsleDividerShadeType.TOP)
        EtollButtonIsle(button = button)
    }
}

@Composable
@Preview
fun EtollButtonIsleWithDividerPreview() {
    EtollButtonIsleWithDivider(
        button = {
            EtollButtonWithDefaultPadding(
                text = "button",
                onClick = { }
            )
        }
    )
}
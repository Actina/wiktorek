package pl.gov.mf.etoll.ui.components.compose.combined.toolbar

import androidx.compose.foundation.Image
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.compose.helpers.getIconInMode

@Composable
fun EtollCloseIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(modifier = modifier, onClick = { onClick() }) {
        Image(painter = painterResource(id =
        getIconInMode(R.drawable.ic_close_new_light,
            R.drawable.ic_close_new_dark)
        ),
            contentDescription = "close icon icon")
    }
}

@Composable
@Preview
fun EtollCloseIconButtonPreview() {
    EtollCloseIconButton {}
}
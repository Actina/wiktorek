package pl.gov.mf.etoll.ui.components.compose.combined.radiobutton

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.compose.helpers.getIconInMode
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens

@Composable
fun EtollRadio(selected: Boolean, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(
            id = if (selected)
                getIconInMode(
                    icLightRes = R.drawable.ic_radio_checked_light,
                    icDarkRes = R.drawable.ic_radio_checked_dark
                )
            else
                R.drawable.ic_radio_unchecked
        ),
        contentDescription = "",
        modifier = modifier.size(Dimens.DEFAULT_IMAGE_RADIO_SIZE, Dimens.DEFAULT_IMAGE_RADIO_SIZE)
    )
}
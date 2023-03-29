package pl.gov.mf.etoll.ui.components.compose.combined.businessnumber

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.compose.helpers.getIconInMode
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.h1Bold
import pl.gov.mf.etoll.ui.components.compose.theme.p2Semibold

@Composable
fun EtollBusinessNumber(
    title: String,
    businessNumber: String,
    onShareAreaClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EtollTheme.colors.businessNumberBackground)
            .padding(
                vertical = Dimens.M_SPACING,
                horizontal = Dimens.PADDING_CONTENT_HORIZONTAL
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            color = EtollTheme.colors.spoeTextContent,
            style = EtollTheme.typography.p2Semibold
        )
        Row(
            modifier = Modifier
                .padding(top = Dimens.M_SPACING)
                .clickable { onShareAreaClicked() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = businessNumber,
                color = EtollTheme.colors.spoeTextContent,
                style = EtollTheme.typography.h1Bold
            )
            Image(
                painter = painterResource(
                    id = getIconInMode(
                        icLightRes = R.drawable.ic_share_blue_light,
                        icDarkRes = R.drawable.ic_share_blue_dark
                    )
                ),
                modifier = Modifier.padding(start = Dimens.M_SPACING),
                contentDescription = "",
            )
        }
    }
}

@Preview
@Composable
fun EtollBusinessNumberPreview() {
    EtollBusinessNumber(
        title = "Twój identyfikator biznesowy",
        businessNumber = "M22-1234F-1",
        onShareAreaClicked = {})
}
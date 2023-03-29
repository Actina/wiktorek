package pl.gov.mf.etoll.ui.components.compose.combined.generic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import pl.gov.mf.etoll.ui.components.compose.theme.*
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButton
import pl.gov.mf.etoll.ui.components.compose.helpers.getIconInMode

@Composable
fun EbiletGenericErrorContentView(
    iconLight: Int?,
    iconDark: Int?,
    titleText: String,
    descriptionText: String,
    titleColor: Color = Color.destructiveRed,
    buttonText: String,
    singleLineTitle: Boolean = true,
    click: () -> Unit,
) {
    Column(
        modifier = Modifier.wrapContentWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (iconLight != null && iconDark != null)
            Image(
                painterResource(getIconInMode(icLightRes = iconLight, icDarkRes = iconDark)),
                contentDescription = "",
                modifier = Modifier
                    .width(Dimens.DEFAULT_INFORMATION_IMAGE_SIZE)
                    .height(Dimens.DEFAULT_INFORMATION_IMAGE_SIZE)
            )
        Text(
            text = titleText,
            style = MaterialTheme.typography.destructive,
            textAlign = TextAlign.Center,
            color = titleColor,
            maxLines = if (singleLineTitle) 1 else 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                top = Dimens.FIELD_VERTICAL_PADDING,
                bottom = Dimens.FIELD_VERTICAL_PADDING
            )
        )
        Column(
            modifier = Modifier
                .padding(bottom = Dimens.TEXT_DEFAULT_VERTICAL_PADDING)
                .weight(weight = 1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = descriptionText,
                color = EtollTheme.colors.spoeTextContent,
                style = MaterialTheme.typography.p1Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        }
        EtollButton(text = buttonText, onClick = click)
    }
}
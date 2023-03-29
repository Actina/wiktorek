package pl.gov.mf.etoll.ui.components.compose.combined.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.gov.mf.etoll.ui.components.compose.combined.generic.EbiletGenericErrorContentView
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.eBiletCardShape

//TODO: Compare with EtollDialog, consider refactor & merge
@Composable
fun EbiletOneButtonComposeDialog(
    iconLight: Int?,
    iconDark: Int?,
    titleText: String,
    descriptionText: String,
    buttonText: String,
    dismissable: Boolean = true,
    titleColor: Color,
    onDismiss: () -> Unit = {},
    click: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = dismissable,
            dismissOnClickOutside = dismissable
        )
    ) {
        Card(
            shape = MaterialTheme.shapes.eBiletCardShape,
            backgroundColor = EtollTheme.colors.dialogBackground
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.XL_SPACING),
                color = EtollTheme.colors.dialogBackground,
            ) {
                EbiletGenericErrorContentView(
                    iconLight = iconLight,
                    iconDark = iconDark,
                    titleText = titleText,
                    titleColor = titleColor,
                    descriptionText = descriptionText,
                    buttonText = buttonText,
                    click = click
                )
            }
        }
    }
}


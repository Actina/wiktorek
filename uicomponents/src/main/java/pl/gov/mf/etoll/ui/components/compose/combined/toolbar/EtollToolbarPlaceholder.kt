package pl.gov.mf.etoll.ui.components.compose.combined.toolbar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens

@Composable
fun EtollToolbarIconPlaceholder() {
    //Note. Width set to MINIMUM_TOUCH_TARGET_SIZE = 48.dp like in case of e.g. IconButton implementation
    Spacer(modifier = Modifier.width(Dimens.MINIMUM_TOUCH_TARGET_SIZE))
}
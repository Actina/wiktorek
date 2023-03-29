package pl.gov.mf.etoll.ui.components.compose.combined.toolbar

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EtollBackIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(modifier = modifier, onClick = { onClick() }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "arrow back icon",
            tint = Color.White
        )
    }
}

@Composable
@Preview
fun EtollBackIconButtonPreview() {
    EtollCloseIconButton {}
}
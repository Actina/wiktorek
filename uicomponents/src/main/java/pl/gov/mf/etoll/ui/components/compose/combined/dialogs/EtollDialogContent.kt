package pl.gov.mf.etoll.ui.components.compose.combined.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EtollDialogContent(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    isleDivider: @Composable ColumnScope.() -> Unit,
    buttonsIsle: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        header()
        title()
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
        isleDivider()
        buttonsIsle()
    }
}

@Composable
@Preview
fun EtollDialogContentPreview() {
    EtollDialogContent(
        header = { Text(text = "header") },
        title = { Text(text = "title") },
        content = { Text(text = "content") },
        isleDivider = { Text(text = "isle divider") },
        buttonsIsle = { Text(text = "button isle") }
    )
}
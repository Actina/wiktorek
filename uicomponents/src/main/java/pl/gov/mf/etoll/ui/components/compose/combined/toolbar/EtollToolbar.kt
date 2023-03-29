package pl.gov.mf.etoll.ui.components.compose.combined.toolbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme

@Composable
fun EtollToolbar(
    startIcon: @Composable RowScope.() -> Unit = {},
    startIconPlaceholder: @Composable RowScope.() -> Unit = {
        EtollToolbarIconPlaceholder()
    },
    showStartIcon: Boolean = false,
    title: @Composable RowScope.() -> Unit,
    endIcon: @Composable RowScope.() -> Unit = {},
    endIconPlaceholder: @Composable RowScope.() -> Unit = {
        EtollToolbarIconPlaceholder()
    },
    showEndIcon: Boolean = false,
) {
    TopAppBar(backgroundColor = EtollTheme.colors.toolbarBackground,
        contentPadding = PaddingValues(start = Dimens.S_SPACING, end = Dimens.S_SPACING)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (showStartIcon) startIcon() else startIconPlaceholder()
            title()
            if (showEndIcon) endIcon() else endIconPlaceholder()
        }
    }
}

@Composable
@Preview
fun EtollToolbarPreview() {
    Column {
        TestBothIcons()
        TestEndIcon()
        TestStartIcon()
        TestNoIcons()
    }

}

@Composable
private fun TestNoIcons() {
    EtollToolbar(
        startIcon = {
            EtollCloseIconButton {
            }
        },
        showStartIcon = false,
        title = {
            EtollToolbarTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = "some title"
            )
        },
        endIcon = {
            EtollCloseIconButton {
            }
        },
        showEndIcon = false
    )
}

@Composable
private fun TestStartIcon() {
    EtollToolbar(
        startIcon = {
            EtollCloseIconButton {
            }
        },
        showStartIcon = true,
        title = {
            EtollToolbarTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = "some title"
            )
        },
        showEndIcon = false
    )
}

@Composable
private fun TestEndIcon() {
    EtollToolbar(
        showStartIcon = false,
        title = {
            EtollToolbarTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = "some title"
            )
        },
        endIcon = {
            EtollCloseIconButton {
            }
        },
        showEndIcon = true
    )
}

@Composable
private fun TestBothIcons() {
    EtollToolbar(
        startIcon = {
            EtollCloseIconButton {
            }
        },
        showStartIcon = true,
        title = {
            EtollToolbarTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = "some title"
            )
        },
        endIcon = {
            EtollCloseIconButton {
            }
        },
        showEndIcon = true
    )
}
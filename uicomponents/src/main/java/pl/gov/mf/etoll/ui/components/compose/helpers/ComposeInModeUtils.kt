package pl.gov.mf.etoll.ui.components.compose.helpers

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme

@Composable
fun getIconInMode(
    @DrawableRes icLightRes: Int,
    @DrawableRes icDarkRes: Int,
) = if (EtollTheme.isDark) icDarkRes else icLightRes
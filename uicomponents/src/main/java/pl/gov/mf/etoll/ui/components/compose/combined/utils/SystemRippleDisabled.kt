package pl.gov.mf.etoll.ui.components.compose.combined.utils

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import pl.gov.mf.etoll.ui.components.compose.theme.NoRippleTheme

@Composable
fun SystemRippleDisabled(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        content()
    }
}
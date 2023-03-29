package pl.gov.mf.etoll.ui.components.compose.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.runtime.*
import pl.gov.mf.etoll.appmode.AppMode

internal val LocalEtollColors = staticCompositionLocalOf<EtollColors> { EtollLightColors() }
internal val LocalIsDarkMode = compositionLocalOf { false }

@Composable
fun EtollTheme(
    getAppMode: () -> AppMode,
    content: @Composable () -> Unit,
) {
    val isDarkMode = remember { getAppMode() == AppMode.DARK_MODE }
    CompositionLocalProvider(
        LocalIsDarkMode provides isDarkMode,
        LocalEtollColors provides if (isDarkMode) EtollDarkColors() else EtollLightColors()
    ) {
        MaterialTheme(colors = if (isDarkMode) nativeColorsDark else nativeColorsLight) {
            content()
        }
    }
}

//By analogy to oryginal MaterialTheme this object has the same name as
//composable corresponding to him and contains functions to access
//the current theme values provided at the call site's position in the hierarchy.
//See also https://developer.android.com/jetpack/compose/designsystems/custom
object EtollTheme {
    val colors: EtollColors
        @Composable get() = LocalEtollColors.current
    val typography: Typography
        @Composable get() = MaterialTheme.typography
    val isDark: Boolean
        @Composable get() = LocalIsDarkMode.current
    val shapes: Shapes
        @Composable get() = MaterialTheme.shapes
}
package pl.gov.mf.etoll.ui.components.compose.theme

import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import com.google.errorprone.annotations.Immutable
import pl.gov.mf.mobile.utils.JsonConvertible

//Copied from eTicket - (TODO: convert gradually to EtollColors or Color.Companion below)
val destructiveRedOverlay = Color(0x88AD0000)
val positiveGreen = Color(0xFA008505)
val warningOrange = Color(0xFFEF7F00)
val DialogAcceptTile = Color(0xFF0052A5)

/*
Pre-defined MaterialTheme colors used by many original native components
 */
val nativeColorsLight = lightColors(
    //Used by default by Material Button
    primary = Color(0xFF0052A5),

    //TODO: adjust/customize colors below
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    secondaryVariant = Color(0xFF121212),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFF121212),
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.Black
)

val nativeColorsDark = lightColors(
    //Used by default by Material Button
    primary = Color(0xFF95BBF9),

    //TODO: adjust/customize colors below
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    secondaryVariant = Color(0xFF121212),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    error = Color(0xFFCF6679),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.Black
)

/*
Unique predefined colors.
Note. They are not mode-aware! If possible use EtollColors instead.
 */
private val Color.Companion.blackBusiness: Color
    get() = Color(0xFF343A40)
private val Color.Companion.lighterDark: Color
    get() = Color(0xFF1F1F1F)
private val Color.Companion.bloodRed: Color
    get() = Color(0xFFDC0032)
val Color.Companion.destructiveRed: Color
    get() = Color(0xFFAD0000)

/*
E-Toll specific colors (mode-aware)
 */
interface EtollColors : JsonConvertible {
    val textColorRed: Color
    val spoeTextContent: Color
    val checkboxForeground: Color
    val checkboxBackground: Color
    val buttonText: Color
    val isleDividerGradientStartColor: Color
    val isleDividerGradientEndColor: Color
    val textPrimary: Color
    val dialogHeaderTextPrimary: Color
    val linkColor: Color
    val toolbarBackground: Color
    val toolbarTextAlwaysWhite: Color
    val buttonIsleBackground: Color
    val dialogBackground: Color
    val businessNumberBackground: Color
    val dialogInfoHeader: Color
    val dialogWarningHeader: Color
    val dialogCriticalHeader: Color
}

@Immutable
data class EtollLightColors(
    override val textColorRed: Color = Color.destructiveRed,
    override val spoeTextContent: Color = Color(0xFF343A40),
    override val checkboxForeground: Color = Color(0xFF343A40),
    override val checkboxBackground: Color = Color.Transparent,
    override val buttonText: Color = Color.White,
    override val isleDividerGradientStartColor: Color = Color.Transparent,
    override val isleDividerGradientEndColor: Color = Color(0xFFE5E5E5),
    override val textPrimary: Color = Color.blackBusiness,
    override val dialogHeaderTextPrimary: Color = Color.blackBusiness,
    override val linkColor: Color = Color(0xFF1652A3),
    override val toolbarBackground: Color = Color.bloodRed,
    override val toolbarTextAlwaysWhite: Color = Color.White,
    override val buttonIsleBackground: Color = Color.White,
    override val dialogBackground: Color = Color.White,
    override val businessNumberBackground: Color = Color.White,
    override val dialogInfoHeader: Color = Color(0xFF0052A5),
    override val dialogWarningHeader: Color = warningOrange,
    override val dialogCriticalHeader: Color = Color.destructiveRed,
) : EtollColors

@Immutable
data class EtollDarkColors(
    override val textColorRed: Color = Color(0xFFE49696),
    override val spoeTextContent: Color = Color(0xFFCCCCCC),
    override val checkboxForeground: Color = Color(0xFFCCCCCC),
    override val checkboxBackground: Color = Color.Transparent,
    override val buttonText: Color = Color.blackBusiness,
    override val isleDividerGradientStartColor: Color = Color.Transparent,
    override val isleDividerGradientEndColor: Color = Color.Transparent,
    override val textPrimary: Color = Color(0xFFB8B8B8),
    override val dialogHeaderTextPrimary: Color = Color(0xFFCCCCCC),
    override val linkColor: Color = Color(0xFF95BBF9),
    override val toolbarBackground: Color = Color.lighterDark,
    override val toolbarTextAlwaysWhite: Color = Color.White,
    override val buttonIsleBackground: Color = Color.lighterDark,
    override val dialogBackground: Color = Color.lighterDark,
    override val businessNumberBackground: Color = Color.lighterDark,
    override val dialogInfoHeader: Color = Color(0xFF95BBF9),
    override val dialogWarningHeader: Color = Color(0xFFF4A952),
    override val dialogCriticalHeader: Color = Color(0xFFE49696),
) : EtollColors

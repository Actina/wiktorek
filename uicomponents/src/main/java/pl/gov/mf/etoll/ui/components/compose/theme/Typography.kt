package pl.gov.mf.etoll.ui.components.compose.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import pl.gov.mf.etoll.ui.components.R

private val MontserratBold = FontFamily(
    Font(
        R.font.montserrat_bold_compose,
        FontWeight.Bold
    ) //FontWeight.Bold is alias for FontWeight.700
)

private val OpenSansRegular = FontFamily(
    Font(R.font.open_sans_regular_compose, FontWeight.W400)
)

private val OpenSansBold = FontFamily(
    Font(R.font.open_sans_bold_compose, FontWeight.Bold)
)

private val OpenSansMedium = FontFamily(
    Font(R.font.open_sans_medium, FontWeight.W600)
)

private val RobotoMediumCompose = FontFamily(
    Font(R.font.roboto_medium_compose, FontWeight.W500)
)

val Typography.toolbarDefault: TextStyle
    get() = TextStyle(
        fontFamily = RobotoMediumCompose,
        fontSize = 20.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp
    )

val Typography.h4BoldHeader: TextStyle
    get() = TextStyle(
        fontFamily = MontserratBold,
        fontStyle = FontStyle.Normal,
        fontSize = 20.sp,
        lineHeight = 30.sp
    )

val Typography.h4bold: TextStyle
    get() = TextStyle(
        fontFamily = MontserratBold,
        fontStyle = FontStyle.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )

val Typography.p2BoldLink: TextStyle
    get() = TextStyle(
        fontFamily = OpenSansBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.019.em
    )

val Typography.h2bold: TextStyle
    get() = TextStyle(
        fontFamily = MontserratBold,
        fontStyle = FontStyle.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp
    )

val Typography.h1Bold: TextStyle
    get() = TextStyle(
        fontFamily = MontserratBold,
        fontStyle = FontStyle.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    )

val Typography.p2regular: TextStyle
    get() = TextStyle(
        fontFamily = OpenSansRegular,
        fontStyle = FontStyle.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.009.em,
    )

val Typography.p2regularLink: TextStyle
    get() = TextStyle(
        fontFamily = OpenSansRegular,
        fontStyle = FontStyle.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.009.em,
    )

val Typography.buttonDefault: TextStyle
    get() = TextStyle(
        fontFamily = OpenSansBold,
        fontStyle = FontStyle.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.019.em
    )

val Typography.p2Semibold: TextStyle
    get() = TextStyle(
        fontFamily = OpenSansMedium,
        fontStyle = FontStyle.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.009.em
    )

val Typography.p1Semibold: TextStyle
    get() = TextStyle(
        fontFamily = OpenSansMedium,
        fontStyle = FontStyle.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.012.em
    )

//TODO: add more fonts

val Typography.p1Bold: TextStyle
    get() = TextStyle(
        fontFamily = OpenSansBold,
        fontStyle = FontStyle.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.019.em
    )

val Typography.p1Normal: TextStyle
    get() = TextStyle(
        fontFamily = OpenSansRegular,
        fontStyle = FontStyle.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.W400
    )


val Typography.destructive: TextStyle
    get() = TextStyle(
        fontFamily = MontserratBold,
        fontStyle = FontStyle.Normal,
        color = Color.destructiveRed,
        fontSize = 28.sp,
        lineHeight = 42.sp
    )
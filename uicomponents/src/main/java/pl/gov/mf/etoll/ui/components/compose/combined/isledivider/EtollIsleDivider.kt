package pl.gov.mf.etoll.ui.components.compose.combined.isledivider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme

@Composable
fun EtollIsleDivider(
    modifier: Modifier = Modifier,
    dividerType: IsleDividerShadeType = IsleDividerShadeType.TOP
) {
    Box(
        modifier = modifier
            .height(Dimens.ISLE_DIVIDER_HEIGHT)
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    startY = when (dividerType) {
                        IsleDividerShadeType.TOP -> 0f
                        IsleDividerShadeType.BOTTOM -> Float.POSITIVE_INFINITY
                    },
                    endY = when (dividerType) {
                        IsleDividerShadeType.TOP -> Float.POSITIVE_INFINITY
                        IsleDividerShadeType.BOTTOM -> 0f
                    },
                    colors = listOf(
                        EtollTheme.colors.isleDividerGradientStartColor,
                        EtollTheme.colors.isleDividerGradientEndColor
                    )
                )
            )
    )
}

enum class IsleDividerShadeType {
    TOP, BOTTOM
}

@Composable
@Preview
fun EtollIsleDividerPreview() {
    EtollIsleDivider()
}
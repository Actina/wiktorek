package pl.gov.mf.etoll.ui.components.compose.combined.checkbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme

@Composable
fun EtollCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onCheckedChange: (Boolean) -> Unit,
) {
    Checkbox(
        modifier = modifier,
        checked = checked,
        onCheckedChange = { onCheckedChange(it) },
        colors = EtollCheckboxColors(checkedBorderColor = EtollTheme.colors.checkboxForeground,
            checkedBoxColor = EtollTheme.colors.checkboxBackground,
            checkedCheckmarkColor = EtollTheme.colors.checkboxForeground,
            uncheckedCheckmarkColor = EtollTheme.colors.checkboxForeground.copy(alpha = 0f),
            uncheckedBoxColor = EtollTheme.colors.checkboxBackground.copy(alpha = 0f),
            disabledCheckedBoxColor = EtollTheme.colors.checkboxBackground,
            disabledUncheckedBoxColor = EtollTheme.colors.checkboxBackground.copy(alpha = 0f),
            disabledIndeterminateBoxColor = EtollTheme.colors.checkboxBackground,
            uncheckedBorderColor = EtollTheme.colors.checkboxForeground,
            disabledBorderColor = EtollTheme.colors.checkboxForeground,
            disabledIndeterminateBorderColor = EtollTheme.colors.checkboxForeground),
        interactionSource = interactionSource)
}

/**
 * Just copied from Checkbox->DefaultCheckboxColors
 * and renamed to EtollCheckboxColors to customize colors
 */
class EtollCheckboxColors(
    private val checkedCheckmarkColor: Color,
    private val uncheckedCheckmarkColor: Color,
    private val checkedBoxColor: Color,
    private val uncheckedBoxColor: Color,
    private val disabledCheckedBoxColor: Color,
    private val disabledUncheckedBoxColor: Color,
    private val disabledIndeterminateBoxColor: Color,
    private val checkedBorderColor: Color,
    private val uncheckedBorderColor: Color,
    private val disabledBorderColor: Color,
    private val disabledIndeterminateBorderColor: Color,
) : CheckboxColors {
    @Composable
    override fun checkmarkColor(state: ToggleableState): State<Color> {
        val target = if (state == ToggleableState.Off) {
            uncheckedCheckmarkColor
        } else {
            checkedCheckmarkColor
        }

        val duration = if (state == ToggleableState.Off) BoxOutDuration else BoxInDuration
        return animateColorAsState(target, tween(durationMillis = duration))
    }

    @Composable
    override fun boxColor(enabled: Boolean, state: ToggleableState): State<Color> {
        val target = if (enabled) {
            when (state) {
                ToggleableState.On, ToggleableState.Indeterminate -> checkedBoxColor
                ToggleableState.Off -> uncheckedBoxColor
            }
        } else {
            when (state) {
                ToggleableState.On -> disabledCheckedBoxColor
                ToggleableState.Indeterminate -> disabledIndeterminateBoxColor
                ToggleableState.Off -> disabledUncheckedBoxColor
            }
        }

        // If not enabled 'snap' to the disabled state, as there should be no animations between
        // enabled / disabled.
        return if (enabled) {
            val duration = if (state == ToggleableState.Off) BoxOutDuration else BoxInDuration
            animateColorAsState(target, tween(durationMillis = duration))
        } else {
            rememberUpdatedState(target)
        }
    }

    @Composable
    override fun borderColor(enabled: Boolean, state: ToggleableState): State<Color> {
        val target = if (enabled) {
            when (state) {
                ToggleableState.On, ToggleableState.Indeterminate -> checkedBorderColor
                ToggleableState.Off -> uncheckedBorderColor
            }
        } else {
            when (state) {
                ToggleableState.Indeterminate -> disabledIndeterminateBorderColor
                ToggleableState.On, ToggleableState.Off -> disabledBorderColor
            }
        }

        // If not enabled 'snap' to the disabled state, as there should be no animations between
        // enabled / disabled.
        return if (enabled) {
            val duration = if (state == ToggleableState.Off) BoxOutDuration else BoxInDuration
            animateColorAsState(target, tween(durationMillis = duration))
        } else {
            rememberUpdatedState(target)
        }
    }
}

private const val BoxInDuration = 50
private const val BoxOutDuration = 100
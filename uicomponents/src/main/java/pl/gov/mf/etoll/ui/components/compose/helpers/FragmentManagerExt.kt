package pl.gov.mf.etoll.ui.components.compose.helpers

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

fun FragmentManager.dialogExists(dialogTag: String): Boolean = findFragmentByTag(dialogTag) != null

fun FragmentManager.dialogNotExists(dialogTag: String): Boolean = !dialogExists(dialogTag)

inline fun <reified T : DialogFragment> FragmentManager.getExistingDialog(dialogTag: String): T? =
    findFragmentByTag(dialogTag)?.let { it as T }
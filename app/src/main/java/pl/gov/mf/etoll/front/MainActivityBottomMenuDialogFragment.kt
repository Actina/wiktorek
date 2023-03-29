package pl.gov.mf.etoll.front

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.MenuMainactivityBinding

class MainActivityBottomMenuDialogFragment : BottomSheetDialogFragment() {

    lateinit var binding: MenuMainactivityBinding

    private val _dialogResult = MutableLiveData<BottomSheetDialogResult>()
    val dialogResult: LiveData<BottomSheetDialogResult>
        get() = _dialogResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Widget_Spoe_BottomSheetDialog_Theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.menu_mainactivity,
            container,
            false
        )
        val view = binding.root

        binding.viewModelSchema = object : BottomSheetDialogViewModelSchema {
            override fun onHelpSelected() {
                _dialogResult.value = BottomSheetDialogResult.HELP
                dismiss()
            }

            override fun onSecuritySelected() {
                _dialogResult.value = BottomSheetDialogResult.SECURITY
                dismiss()
            }

            override fun onSettingsSelected() {
                _dialogResult.value = BottomSheetDialogResult.SETTINGS
                dismiss()
            }

            override fun onAboutSelected() {
                _dialogResult.value = BottomSheetDialogResult.ABOUT
                dismiss()
            }


        }

        return view
    }

    interface BottomSheetDialogViewModelSchema {
        fun onHelpSelected()
        fun onSecuritySelected()
        fun onSettingsSelected()
        fun onAboutSelected()
    }

    enum class BottomSheetDialogResult {
        DISMISSED,
        HELP,
        SECURITY,
        SETTINGS,
        ABOUT
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        _dialogResult.value = BottomSheetDialogResult.DISMISSED
    }

    companion object {

        fun createAndShow(
            fm: FragmentManager
        ): MainActivityBottomMenuDialogFragment =
            MainActivityBottomMenuDialogFragment().apply {
                show(fm, "MainMenuMenu")
            }
    }
}
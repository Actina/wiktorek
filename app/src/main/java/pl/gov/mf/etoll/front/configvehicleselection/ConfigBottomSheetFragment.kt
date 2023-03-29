package pl.gov.mf.etoll.front.configvehicleselection

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.model.CoreAccountTypes
import pl.gov.mf.etoll.core.model.CoreVehicle
import pl.gov.mf.etoll.databinding.ConfigVehicleSelectionBottomSheetBinding
import pl.gov.mf.mobile.utils.toObject

class ConfigVehicleSelectionBottomSheetFragment : BottomSheetDialogFragment() {

    private val _dialogResult = MutableLiveData<BottomSheetDialogResult>()
    val dialogResult: LiveData<BottomSheetDialogResult>
        get() = _dialogResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Widget_Spoe_BottomSheetDialog_Theme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                (this@ConfigVehicleSelectionBottomSheetFragment.dialog as BottomSheetDialog).behavior.state =
                    BottomSheetBehavior.STATE_EXPANDED
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: ConfigVehicleSelectionBottomSheetBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.config_vehicle_selection_bottom_sheet,
            container,
            false
        )
        val view = binding.root
        arguments?.getString(SELECTED_VEHICLE)?.toObject<CoreVehicle>()?.let {
            binding.viewModelSchema = object : BottomSheetDialogViewModelSchema {
                override val selectedVehicle: CoreVehicle
                    get() = it

                override val untranslatedAccountType: String
                    get() = CoreAccountTypes.toUiLiteral(selectedVehicle.accountInfo.type)

                override fun onChooseSelected() {
                    _dialogResult.value = BottomSheetDialogResult.CHOSEN
                    dismiss()
                }

                override fun onCancelSelected() {
                    _dialogResult.value = BottomSheetDialogResult.CANCELED
                    dismiss()
                }
            }
        }
        return view
    }

    interface BottomSheetDialogViewModelSchema {
        val selectedVehicle: CoreVehicle
        val untranslatedAccountType: String
        fun onChooseSelected()
        fun onCancelSelected()
    }

    enum class BottomSheetDialogResult {
        DISMISSED,
        CANCELED,
        CHOSEN,
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        _dialogResult.value = BottomSheetDialogResult.DISMISSED
    }

    companion object {
        const val SELECTED_VEHICLE = "coreVehicle"

        fun createAndShow(
            fm: FragmentManager,
            coreVehicle: String
        ): ConfigVehicleSelectionBottomSheetFragment =
            ConfigVehicleSelectionBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(SELECTED_VEHICLE, coreVehicle)
                }
                show(fm, "RideConfigurationBottomSheetDialog")
            }
    }
}
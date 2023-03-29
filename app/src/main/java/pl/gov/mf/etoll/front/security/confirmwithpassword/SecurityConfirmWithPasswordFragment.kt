package pl.gov.mf.etoll.front.security.confirmwithpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentSecurityConfirmWithPasswordBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.security.config.types.SecurityConfigurationType
import pl.gov.mf.etoll.front.security.confirmwithpassword.types.ConfirmWithPasswordOperationType
import pl.gov.mf.etoll.interfaces.WarningsBasicLevels
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.BUTTON_TEXT_RES
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.CONTENT_TEXT_RES
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.HEADER_TEXT_RES
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.ICON_TYPE

class SecurityConfirmWithPasswordFragment :
    BaseDatabindingFragment<SecurityConfirmWithPasswordViewModel, SecurityConfirmWithPasswordFragmentComponent>() {

    private var confirmationType = ConfirmWithPasswordOperationType.PIN_RESET

    private lateinit var binding: FragmentSecurityConfirmWithPasswordBinding

    override fun createComponent(): SecurityConfirmWithPasswordFragmentComponent =
        activityComponent.plus(
            SecurityConfirmWithPasswordFragmentModule(this, lifecycle)
        )

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_security_confirm_with_password,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getConfirmationType()

        binding.securityConfirmWithPasswordToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.navDirections.observe(viewLifecycleOwner) {
            when (it) {
                SecurityConfirmWithPasswordViewModel.NavigationTarget.NONE -> Unit
                SecurityConfirmWithPasswordViewModel.NavigationTarget.SETTINGS -> navigateToPinSettings()
                SecurityConfirmWithPasswordViewModel.NavigationTarget.INVALID_PASSWORD -> showInvalidPasswordDialog()
                SecurityConfirmWithPasswordViewModel.NavigationTarget.BACK -> findNavController().popBackStack()
                else -> Unit
            }
        }
    }

    override fun shouldSecureThisFragment(): Boolean = true

    private fun navigateToPinSettings() {
        findNavController().navigate(
            SecurityConfirmWithPasswordFragmentDirections.actionShowSecurityConfigPin(
                signComponentUseCase.executeCustomSign(),
                SecurityConfigurationType.SETTINGS.ordinal
            )
        )
        viewModel.clearNavigation()
    }

    private fun getConfirmationType() {
        try {
            val confirmationType =
                ConfirmWithPasswordOperationType.values()[arguments?.getInt("confirmationType")
                    ?: confirmationType.ordinal]

            viewModel.setConfirmationType(confirmationType)
        } catch (ex: Exception) {
            //Ignore
        }
    }

    private fun showInvalidPasswordDialog() =
        SecurityConfigValidationDialogFragment
            .createAndShow(childFragmentManager, Bundle().apply {
                putString(HEADER_TEXT_RES, "dialog_invalid_confirmation_password_header")
                putString(CONTENT_TEXT_RES, "dialog_invalid_confirmation_password_content")
                putString(BUTTON_TEXT_RES, "dialog_invalid_confirmation_password_ok")
                putString(ICON_TYPE, WarningsBasicLevels.YELLOW.getName())
            })
}
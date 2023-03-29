package pl.gov.mf.etoll.front.security.config.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentSecurityConfigPinBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.security.config.types.SecurityConfigurationType
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.BUTTON_TEXT_RES
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.CONTENT_TEXT_RES
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.HEADER_TEXT_RES
import pl.gov.mf.etoll.validations.ValidatedRule
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment
import pl.gov.mf.mobile.ui.components.utils.hideKeyboard
import pl.gov.mf.mobile.ui.components.views.pin.PinInput

class ConfigPinFragment :
    BaseDatabindingFragment<ConfigPinViewModel, ConfigPinFragmentComponent>() {

    private lateinit var binding: FragmentSecurityConfigPinBinding
    private lateinit var configSecurityPinInput: PinInput
    private lateinit var configSecurityPinConfirmationPinInput: PinInput

    override fun createComponent(): ConfigPinFragmentComponent =
        activityComponent.plus(
            ConfigPinFragmentModule(
                this,
                lifecycle
            )
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)

        if (viewModel.securityTransSucceedAndIsRegistration())
            showDashboard()
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        configSecurityPinInput.invalidateUncommonItems()
        configSecurityPinConfirmationPinInput.invalidateUncommonItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_security_config_pin,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setupView(binding.root)
        return binding.root
    }

    private fun setupView(view: View) {
        configSecurityPinInput = view.findViewById(R.id.config_security_pin_input)
        configSecurityPinConfirmationPinInput =
            view.findViewById(R.id.config_security_pin_confirmation_pin_input)

    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        getConfigurationType()

        lockBackPress(viewModel::onBackPressed)
        view.findViewById<MaterialToolbar>(R.id.config_security_pin_toolbar)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        setupPinInputs()

        viewModel.navigate.observe(viewLifecycleOwner) {
            when (it) {
                ConfigPinViewModel.NavigationTargets.NONE -> Unit
                ConfigPinViewModel.NavigationTargets.CONFIG_PASSWORD -> showSecurityConfigPassword()
                ConfigPinViewModel.NavigationTargets.DASHBOARD -> showDashboard()
                ConfigPinViewModel.NavigationTargets.SECURITY_SETTINGS ->
                    navigateBackToSecuritySettings()
                ConfigPinViewModel.NavigationTargets.EXIT_RESET_PROCESS -> exitResetProcess()
                else -> Unit
            }
        }

        viewModel.validationResult.observe(viewLifecycleOwner) { validationResult ->
            if (validationResult.isValid) return@observe

            configSecurityPinInput.clearPin()
            configSecurityPinConfirmationPinInput.clearPin()

            when (validationResult.validatedRule) {
                ValidatedRule.EQUAL -> showValidationDialog(
                    "dialog_mismatched_pin_codes_header",
                    "dialog_mismatched_pin_codes_content",
                    "dialog_mismatched_pin_codes_retry"
                )
                ValidatedRule.MIN_LENGTH -> showValidationDialog(
                    "dialog_invalid_pin_code_header",
                    "dialog_invalid_pin_code_content",
                    "dialog_invalid_pin_code_retry"
                )
                else -> Unit
            }
        }
    }

    override fun shouldSecureThisFragment(): Boolean = true

    private fun exitResetProcess() =
        findNavController().popBackStack(R.id.securityUnlockWithPinCodeFragment, true)

    private fun navigateBackToSecuritySettings() =
        findNavController().popBackStack(R.id.appSecuritySettingsFragment, false)

    private fun showValidationDialog(
        headerTextResource: String,
        contentTextResource: String,
        buttonTextResource: String,
    ) = SecurityConfigValidationDialogFragment
        .createAndShow(childFragmentManager, Bundle().apply {
            putString(HEADER_TEXT_RES, headerTextResource)
            putString(CONTENT_TEXT_RES, contentTextResource)
            putString(BUTTON_TEXT_RES, buttonTextResource)
        })
        ?.observe(viewLifecycleOwner) {
            if (it == BasicOneActionDialogFragment.DialogResult.CONFIRMED)
                viewModel.clearValidationResult()
        }

    private fun showDashboard() = findNavController().navigate(
        ConfigPinFragmentDirections.actionShowDashboard(
            signComponentUseCase.executeCustomSign()
        )
    )

    private fun showSecurityConfigPassword() {
        findNavController().navigate(
            ConfigPinFragmentDirections.actionSecurityConfigPassword(
                signComponentUseCase.executeCustomSign(),
                viewModel.configurationType.ordinal
            )
        )
        clearScreen()
    }

    private fun clearScreen() {
        viewModel.resetNavigation()
        configSecurityPinInput.clearPin()
        configSecurityPinConfirmationPinInput.clearPin()
    }

    private fun setupPinInputs() {
        lifecycle.addObserver(configSecurityPinInput)
        lifecycle.addObserver(configSecurityPinConfirmationPinInput)

        configSecurityPinInput.setOnPinReady {
            configSecurityPinConfirmationPinInput.performClick()
        }

        configSecurityPinConfirmationPinInput.setOnPinReady {
            configSecurityPinConfirmationPinInput.hideKeyboard()
        }
    }

    private fun getConfigurationType() {
        try {
            viewModel.configurationType =
                SecurityConfigurationType.values()[arguments?.getInt("configurationType") ?: 0]
        } catch (ex: Exception) {
            //Ignore
        }
    }
}

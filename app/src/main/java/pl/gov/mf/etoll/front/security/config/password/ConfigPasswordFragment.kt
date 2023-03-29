package pl.gov.mf.etoll.front.security.config.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.androidsettings.AndroidSettingsManagerUC
import pl.gov.mf.etoll.core.biometric.BiometricAuthManagerUC
import pl.gov.mf.etoll.core.biometric.BiometricStatus
import pl.gov.mf.etoll.databinding.FragmentSecurityConfigPasswordBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.security.config.types.SecurityConfigurationType
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.BUTTON_TEXT_RES
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.CONTENT_TEXT_RES
import pl.gov.mf.etoll.ui.components.dialogs.SecurityConfigValidationDialogFragment.Companion.HEADER_TEXT_RES
import pl.gov.mf.etoll.validations.ValidatedRule
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment
import javax.inject.Inject


class ConfigPasswordFragment :
    BaseDatabindingFragment<ConfigPasswordViewModel, ConfigPasswordFragmentComponent>() {

    @Inject
    lateinit var performBiometricAuthentication: BiometricAuthManagerUC.PerformBiometricAuthentication

    @Inject
    lateinit var openSecuritySettingsUseCase: AndroidSettingsManagerUC.OpenSecuritySettingsUseCase

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    private lateinit var binding: FragmentSecurityConfigPasswordBinding

    private lateinit var configSecurityPasswordBiometricSwitch: SwitchMaterial

    override fun createComponent(): ConfigPasswordFragmentComponent =
        activityComponent.plus(
            ConfigPasswordFragmentModule(
                this,
                lifecycle
            )
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
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_security_config_password,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        configSecurityPasswordBiometricSwitch =
            binding.root.findViewById(R.id.config_security_password_biometric_switch)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        getConfigurationType()

        view.findViewById<MaterialToolbar>(R.id.config_security_password_toolbar)
            .setNavigationOnClickListener {
                findNavController().popBackStack()
            }

        configSecurityPasswordBiometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onBiometricSettingsChanged(isChecked)
        }

        viewModel.validationResult.observe(viewLifecycleOwner) { validationResult ->
            if (validationResult.isValid) return@observe

            when (validationResult.validatedRule) {
                ValidatedRule.EQUAL -> showValidationDialog(
                    "dialog_mismatched_passwords_header",
                    "dialog_mismatched_passwords_content",
                    "dialog_mismatched_passwords_retry"
                )
                ValidatedRule.MIN_LENGTH,
                ValidatedRule.CONTAINS_LETTER,
                ValidatedRule.CONTAINS_DIGIT -> showValidationDialog(
                    "dialog_invalid_password_header",
                    "dialog_invalid_password_content",
                    "dialog_invalid_password_retry"
                )
                else -> Unit
            }

        }

        viewModel.navigate.observe(viewLifecycleOwner) { navigationTargets ->
            when (navigationTargets) {
                ConfigPasswordViewModel.NavigationTargets.NONE -> Unit
                ConfigPasswordViewModel.NavigationTargets.DASHBOARD -> findNavController().navigate(
                    ConfigPasswordFragmentDirections.actionShowDashboard(
                        signComponentUseCase.executeCustomSign()
                    )
                )
                ConfigPasswordViewModel.NavigationTargets.SECURITY_SETTINGS ->
                    findNavController().popBackStack(R.id.appSecuritySettingsFragment, false)
                ConfigPasswordViewModel.NavigationTargets.EXIT_RESET_PROCESS ->
                    findNavController().popBackStack(R.id.securityUnlockWithPinCodeFragment, true)
                ConfigPasswordViewModel.NavigationTargets.BIOMETRIC_SETTINGS -> goToSecuritySettings()
                ConfigPasswordViewModel.NavigationTargets.BIOMETRIC_PROMPT -> performBiometricAuth()
                else -> Unit
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkBiometricState()
    }

    override fun shouldSecureThisFragment(): Boolean = true

    private fun showValidationDialog(
        headerTextResource: String,
        contentTextResource: String,
        buttonTextResource: String
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

    private fun goToSecuritySettings() =
        dialogsHelper.showTranslatedOkDialog(
            childFragmentManager,
            "dialog_open_biometric_settings_header",
            "dialog_open_biometric_settings_content",
            "dialog_open_biometric_settings_ok"
        )
            .subscribe({
                configSecurityPasswordBiometricSwitch.isChecked = false
                openSecuritySettingsUseCase.execute()
            }, {})

    private fun performBiometricAuth() =
        performBiometricAuthentication.execute(
            this,
            "dialog_biometric_prompt_config_title_android",
            "dialog_biometric_prompt_config_cancel_android"
        )
            .subscribe({ result ->
                when (result) {
                    BiometricStatus.SUCCESS -> {
                        viewModel.saveBiometricValueToSettings(true)
                    }
                    BiometricStatus.CANCEL ->
                        configSecurityPasswordBiometricSwitch.isChecked = false
                    else -> Unit
                }
            }, {})

    private fun getConfigurationType() {
        try {
            val configurationType =
                SecurityConfigurationType.values()[arguments?.getInt("configurationType") ?: 0]

            viewModel.setConfigurationType(configurationType)
        } catch (ex: Exception) {
            //Ignore
        }
    }

}

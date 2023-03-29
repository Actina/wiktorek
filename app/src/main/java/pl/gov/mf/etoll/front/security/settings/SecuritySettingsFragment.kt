package pl.gov.mf.etoll.front.security.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.androidsettings.AndroidSettingsManagerUC
import pl.gov.mf.etoll.core.biometric.BiometricAuthManagerUC
import pl.gov.mf.etoll.core.biometric.BiometricStatus
import pl.gov.mf.etoll.core.devicecompatibility.types.BiometricState
import pl.gov.mf.etoll.databinding.FragmentSecuritySettingsBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.security.config.types.SecurityConfigurationType
import pl.gov.mf.etoll.front.security.confirmwithpassword.types.ConfirmWithPasswordOperationType
import pl.gov.mf.etoll.front.security.settings.adapteritem.BiometricConfigItem
import pl.gov.mf.etoll.front.security.settings.adapteritem.PinConfigItem
import pl.gov.mf.etoll.front.security.settings.adapteritem.SecurityConfigItem
import pl.gov.mf.etoll.front.security.settings.models.SecuritySettingsConfig
import pl.gov.mf.etoll.utils.DynamicBindingAdapter
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import javax.inject.Inject

class SecuritySettingsFragment :
    BaseDatabindingFragment<SecuritySettingsViewModel, SecuritySettingsFragmentComponent>() {

    @Inject
    lateinit var performBiometricAuthentication: BiometricAuthManagerUC.PerformBiometricAuthentication

    @Inject
    lateinit var openSecuritySettingsUseCase: AndroidSettingsManagerUC.OpenSecuritySettingsUseCase

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    private lateinit var binding: FragmentSecuritySettingsBinding
    private lateinit var adapter: DynamicBindingAdapter

    override fun getBindings(): ViewDataBinding? = binding

    override fun createComponent(): SecuritySettingsFragmentComponent =
        activityComponent.plus(
            SecuritySettingsFragmentModule(this, lifecycle)
        )

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
            inflater,
            R.layout.fragment_security_settings,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = DynamicBindingAdapter(viewLifecycleOwner)
        binding.securityOverviewRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.security_settings_toolbar)
            .setNavigationOnClickListener {
                findNavController().popBackStack()
            }

        viewModel.checkBiometricState()
        viewModel.checkSecurityConfig()
        viewModel.fetchBiometricAuthOption()

        viewModel.securitySettingsConfig.observe(viewLifecycleOwner) { securitySettingsConfig ->
            if (securitySettingsConfig.isBiometricAvailable != null && securitySettingsConfig.isSecurityConfigValid != null)
                adapter.submitList(regenerateAdapterItems(securitySettingsConfig))
        }
    }

    private fun regenerateAdapterItems(
        securitySettingsConfig: SecuritySettingsConfig
    ): List<DynamicBindingAdapter.Item> {
        if (securitySettingsConfig.isSecurityConfigValid == false)
            return listOf(
                SecurityConfigItem(::navigateToSecurityConfigPin)
            )

        val items = mutableListOf<DynamicBindingAdapter.Item>()
        items.add(
            PinConfigItem(
                { navigateToConfirmWithPassword(ConfirmWithPasswordOperationType.PIN_RESET) },
                { navigateToConfirmWithPassword(ConfirmWithPasswordOperationType.SECURITY_SWITCHING_OFF) }
            )
        )

        if (securitySettingsConfig.isBiometricAvailable == true)
            items.add(
                BiometricConfigItem(viewModel.isBiometricAuthOn) {
                    val isEnabled = viewModel.toggleBiometricAuthOption()
                    onBiometricSettingsChanged(isEnabled)
                }
            )

        return items
    }

    private fun navigateToSecurityConfigPin() =
        findNavController().navigate(
            SecuritySettingsFragmentDirections.actionShowSecurityConfigPin(
                signComponentUseCase.executeCustomSign(),
                SecurityConfigurationType.SETTINGS.ordinal
            )
        )

    private fun onBiometricSettingsChanged(isEnabled: Boolean) {
        if (!isEnabled) {
            viewModel.saveBiometricValueToSettings(false)
            return
        }

        when (viewModel.biometricState.value) {
            BiometricState.AVAILABLE_BUT_SWITCHED_OFF -> goToSecuritySettings()
            BiometricState.AVAILABLE_AND_SWITCHED_ON -> performBiometricAuth()
            else -> Unit
        }
    }

    private fun goToSecuritySettings() =
        dialogsHelper.showTranslatedOkDialog(
            childFragmentManager,
            "dialog_open_biometric_settings_header",
            "dialog_open_biometric_settings_content",
            "dialog_open_biometric_settings_ok"
        )
            .subscribe({
                viewModel.switchOffBiometricAuthOption()
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
                    BiometricStatus.SUCCESS -> viewModel.saveBiometricValueToSettings(true)
                    BiometricStatus.ERROR, BiometricStatus.CANCEL -> viewModel.switchOffBiometricAuthOption()
                    else -> Unit
                }
            }, {})

    private fun navigateToConfirmWithPassword(operationType: ConfirmWithPasswordOperationType) =
        findNavController().navigate(
            SecuritySettingsFragmentDirections.actionShowConfirmWithPassword(
                signComponentUseCase.executeCustomSign(),
                operationType.ordinal
            )
        )
}
package pl.gov.mf.etoll.front.security.resettounlock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentSecurityResetPinCodeToUnlockBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.security.config.types.SecurityConfigurationType
import pl.gov.mf.etoll.front.security.confirmwithpassword.SecurityConfirmWithPasswordFragmentDirections

class SecurityResetPinCodeToUnlockFragment :
    BaseDatabindingFragment<SecurityResetPinCodeToUnlockViewModel, SecurityResetPinCodeToUnlockFragmentComponent>() {

    private lateinit var binding: FragmentSecurityResetPinCodeToUnlockBinding

    override fun createComponent(): SecurityResetPinCodeToUnlockFragmentComponent =
        activityComponent.plus(
            SecurityResetPinCodeToUnlockFragmentModule(this, lifecycle)
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
            inflater,
            R.layout.fragment_security_reset_pin_code_to_unlock,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigate.observe(viewLifecycleOwner) { navigationTargets ->
            when (navigationTargets) {
                SecurityResetPinCodeToUnlockViewModel.NavigationTargets.NONE -> Unit
                SecurityResetPinCodeToUnlockViewModel.NavigationTargets.CONFIG_SECURITY -> navigateToConfigureSecurity()
                else -> Unit
            }
        }

        binding.securityResetPinCodeToUnlockToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        lockBackPress {
            if (viewModel.isProcessLocked()) closeApp()
            else findNavController().popBackStack()
        }
    }

    override fun shouldSecureThisFragment(): Boolean = true

    private fun navigateToConfigureSecurity() {
        findNavController().navigate(
            SecurityConfirmWithPasswordFragmentDirections.actionShowSecurityConfigPin(
                signComponentUseCase.executeCustomSign(),
                SecurityConfigurationType.RESET_TO_UNLOCK.ordinal
            )
        )
    }
}
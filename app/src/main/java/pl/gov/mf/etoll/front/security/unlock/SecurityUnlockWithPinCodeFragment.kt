package pl.gov.mf.etoll.front.security.unlock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.MainNavgraphDirections
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.core.biometric.BiometricAuthManagerUC
import pl.gov.mf.etoll.databinding.FragmentSecurityUnlockWithPinCodeBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.mobile.ui.components.utils.hideKeyboard
import javax.inject.Inject

class SecurityUnlockWithPinCodeFragment :
    BaseDatabindingFragment<SecurityUnlockWithPinCodeViewModel, SecurityUnlockWithPinCodeFragmentComponent>() {

    @Inject
    lateinit var performBiometricAuthentication: BiometricAuthManagerUC.PerformBiometricAuthentication

    private lateinit var binding: FragmentSecurityUnlockWithPinCodeBinding

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        minimizeOnBackPress()

        viewModel.markAppAsLocked()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_security_unlock_with_pin_code,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPinInput()

        viewModel.navigate.observe(viewLifecycleOwner) { navigationTargets ->
            when (navigationTargets) {
                SecurityUnlockWithPinCodeViewModel.NavigationTargets.NONE -> Unit
                SecurityUnlockWithPinCodeViewModel.NavigationTargets.BACK -> findNavController().popBackStack()
                else -> Unit
            }
        }

        binding.securityUnlockWithPinCodeBiometricSignInButton.setOnClickListener {
            performBiometricAuth()
        }

        binding.securityUnlockWithPinCodeForgottenPinButton.setOnClickListener {
            findNavController().navigate(
                MainNavgraphDirections.actionShowResetPinCodeToUnlock(
                    signComponentUseCase.executeCustomSign()
                )
            )
        }
    }

    override fun createComponent(): SecurityUnlockWithPinCodeFragmentComponent =
        activityComponent.plus(
            SecurityUnlockWithPinCodeFragmentModule(this, lifecycle)
        )

    override fun shouldSecureThisFragment(): Boolean = true

    private fun setupPinInput() {
        lifecycle.addObserver(binding.securityUnlockWithPinCodePinInput)

        binding.securityUnlockWithPinCodePinInput.setOnPinReady {
            binding.securityUnlockWithPinCodePinInput.hideKeyboard()
            viewModel.checkPin()
        }
    }

    private fun performBiometricAuth() =
        performBiometricAuthentication.execute(
            this,
            "dialog_biometric_prompt_sign_in_title_android",
            "dialog_biometric_prompt_sign_in_cancel_android"
        )
            .subscribe(viewModel::handleBiometricResult, {})
}
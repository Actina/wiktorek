package pl.gov.mf.etoll.core.biometric

import androidx.fragment.app.Fragment
import io.reactivex.android.schedulers.AndroidSchedulers

sealed class BiometricAuthManagerUC {

    class PerformBiometricAuthentication(
        private val biometricAuthManager: BiometricAuthManager
    ) : BiometricAuthManagerUC() {

        fun execute(fragment: Fragment, titleResource: String, negativeTextResource: String) =
            biometricAuthManager.openPrompt(fragment, titleResource, negativeTextResource)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
package pl.gov.mf.etoll.core.biometric

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject

class BiometricAuthManagerImpl @Inject constructor() : BiometricAuthManager {

    override fun openPrompt(
        fragment: Fragment,
        titleResource: String,
        negativeTextResource: String
    ): Observable<BiometricStatus> {
        val subject = PublishSubject.create<BiometricStatus>()
        val executor = ContextCompat.getMainExecutor(fragment.requireContext())

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                subject.onNext(BiometricStatus.ERROR)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                subject.onNext(BiometricStatus.CANCEL)
                subject.onComplete()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                subject.onNext(BiometricStatus.SUCCESS)
                subject.onComplete()
            }
        }

        val biometricPrompt = BiometricPrompt(fragment, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titleResource.translate(fragment.requireContext()))
            .setNegativeButtonText(negativeTextResource.translate(fragment.requireContext()))
            .build()

        biometricPrompt.authenticate(promptInfo)

        return subject
    }

}
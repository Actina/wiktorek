package pl.gov.mf.etoll.core.biometric

import androidx.fragment.app.Fragment
import io.reactivex.Observable

interface BiometricAuthManager {

    fun openPrompt(
        fragment: Fragment,
        titleResource: String,
        negativeTextResource: String
    ): Observable<BiometricStatus>
}
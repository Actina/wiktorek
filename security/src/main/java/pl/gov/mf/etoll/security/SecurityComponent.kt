package pl.gov.mf.etoll.security

import pl.gov.mf.etoll.security.SecurityUC.*
import pl.gov.mf.etoll.security.checker.SecuritySanityCheckerUseCase.*

interface SecurityComponent {

    fun useCaseGetDeviceId(): GenerateDeviceIdUseCase
    fun useCaseGetMasterKey(): GetMasterKeyUseCase
    fun useCaseSignAndroidComponent(): SignAndroidComponentUseCase
    fun useCaseValidateSigningOfAndroidComponent(): ValidateAndroidComponentSigningUseCase
    fun useCaseValidateSecuritySanity(): ValidateSecuritySanityUseCase
    fun useCaseGetAppSigning(): GetAppSigningUseCase

}

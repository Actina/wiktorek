package pl.gov.mf.etoll.security.checker

import android.content.Intent
import android.os.Bundle
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.gov.mf.etoll.security.checker.root.DeviceRootChecker

sealed class SecuritySanityCheckerUseCase {

    class SignAndroidComponentUseCase(private val ds: SecuritySanityChecker) :
        SecuritySanityCheckerUseCase() {
        fun execute(intent: Intent): Intent {
            return ds.sign(intent)
        }

        fun execute(bundle: Bundle): Bundle {
            return ds.sign(bundle)
        }

        fun executeCustomSign(): String = ds.getSigning()
    }

    class ValidateAndroidComponentSigningUseCase(private val ds: SecuritySanityChecker) :
        SecuritySanityCheckerUseCase() {
        fun execute(intent: Intent): Boolean {
            return ds.checkIfIntentIsSigned(intent)
        }

        fun execute(bundle: Bundle): Boolean {
            return ds.checkIfBundleIsSigned(bundle)
        }

        fun executeAsync(intent: Intent): Single<Boolean> {
            return Single.just(ds.checkIfIntentIsSigned(intent)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        fun executeAsync(bundle: Bundle): Single<Boolean> {
            return Single.just(ds.checkIfBundleIsSigned(bundle)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    class ValidateSecuritySanityUseCase(private val ds: SecuritySanityChecker) :
        SecuritySanityCheckerUseCase() {
        fun execute(testMode: Boolean = false): Single<List<SecurityIssues>> {
            return ds.validateSecuritySanity(testMode).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    class CheckIfDeviceIsRootedUseCase(private val deviceRootChecker: DeviceRootChecker) {
        fun executeAsync(): Single<Boolean> =
            deviceRootChecker.isRoot().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
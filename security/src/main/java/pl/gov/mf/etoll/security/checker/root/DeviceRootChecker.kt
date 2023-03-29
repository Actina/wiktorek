package pl.gov.mf.etoll.security.checker.root

import io.reactivex.Single

interface DeviceRootChecker {
    fun isRoot(): Single<Boolean>
}
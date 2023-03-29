package pl.gov.mf.etoll.core.connection

import io.reactivex.Observable

interface InternetConnectionStateController {
    fun intervalCheck()
    fun observeConnectionState(): Observable<Boolean>
}
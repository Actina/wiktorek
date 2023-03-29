package pl.gov.mf.etoll.core.inactivity

import io.reactivex.Single

interface InactivityController {
    fun onAppGoesToBackground()
    fun onAppGoesToForeground()
    fun onCheckRequested(): Single<InactivityState>
}
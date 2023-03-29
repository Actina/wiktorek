package pl.gov.mf.etoll.utils

import pl.gov.mf.etoll.BuildConfig
import pl.gov.mf.etoll.core.watchdog.minversion.BuildVersionProvider
import javax.inject.Inject

class BuildVersionProviderImpl @Inject constructor() : BuildVersionProvider {
    override fun getCurrentBuildVersion(): Int = BuildConfig.VERSION_CODE
}
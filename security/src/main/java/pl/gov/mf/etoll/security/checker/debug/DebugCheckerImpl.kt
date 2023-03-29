package pl.gov.mf.etoll.security.checker.debug

import android.content.Context
import android.content.pm.ApplicationInfo
import pl.gov.mf.etoll.security.BuildConfig
import javax.inject.Inject

class DebugCheckerImpl @Inject constructor(private val context: Context) : DebugChecker {
    override fun checkIfDebuggingIsEnabled(testMode: Boolean): Boolean {
        // we check issues only in non-debug release. For debug one, debugging should also be enabled
        // we're avoiding BuildConfig.DEBUG as it could be tracked easier in byte code than custom value
        return if (BuildConfig.ALLOW_DBG && !testMode) false
        else ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0)
    }
}
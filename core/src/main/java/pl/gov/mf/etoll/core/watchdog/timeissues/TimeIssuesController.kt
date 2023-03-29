package pl.gov.mf.etoll.core.watchdog.timeissues

import pl.gov.mf.mobile.networking.api.interceptors.TimeIssuesTriggersCallbacks

interface TimeIssuesController : TimeIssuesTriggersCallbacks {
    fun onTimeIssueDetected(forceShow: Boolean)
    fun onTimeIssueScreenShown()
    fun setCallbacks(callbacks: TimeIssuesCallbacks, tag: String)
    fun removeCallbacks(tag: String)
    fun intervalCheck()

    companion object {
        const val MIN_TIME_IN_SECS_BETWEEN_MESSAGES = 180L // ~3 mins
    }
}

interface TimeIssuesCallbacks {
    fun showTimeIssueScreen()
}
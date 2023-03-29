package pl.gov.mf.etoll.front.tecs.transaction

import pl.gov.mf.etoll.BuildConfig

object TransactionConfiguration {
    const val TECS_TIMER_BONUS_AFTER_RETURNING_FROM_BACKGROUND = 2

    val TECS_TIMER_LIMIT_IN_SECS = if (BuildConfig.DEBUG) 60L else 60 * 10L // 10 / 1min
    val TECS_TIMER_LIMIT_VISIBILITY_FROM_SECS =
        if (BuildConfig.DEBUG) 30L else 60 * 5L // 5 / 0.5min
}
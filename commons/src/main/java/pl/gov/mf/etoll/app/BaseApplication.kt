package pl.gov.mf.etoll.app

import android.content.Intent
import pl.gov.mf.etoll.commons.CommonsComponent

interface BaseApplication {
    fun getApplicationComponent(): CommonsComponent
    fun getForegroundActionIntent(): Intent
}
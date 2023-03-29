package pl.gov.mf.etoll.core.app

import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.core.app.di.ApplicationComponent

interface NkspoApplication : BaseApplication {
    override fun getApplicationComponent(): ApplicationComponent
}
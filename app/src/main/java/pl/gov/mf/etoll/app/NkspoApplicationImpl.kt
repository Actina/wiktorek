package pl.gov.mf.etoll.app

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.multidex.MultiDexApplication
import org.joda.time.DateTime
import pl.gov.mf.etoll.app.di.ApplicationComponentImpl
import pl.gov.mf.etoll.app.di.ApplicationModule
import pl.gov.mf.etoll.app.di.DaggerApplicationComponentImpl
import pl.gov.mf.etoll.app.timeshift.TimeShiftAdapter
import pl.gov.mf.etoll.commons.CommonsModule
import pl.gov.mf.etoll.core.CoreModule
import pl.gov.mf.etoll.core.app.NkspoApplication
import pl.gov.mf.etoll.core.app.di.ApplicationComponent
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.logging.LogUseCase
import pl.gov.mf.etoll.networking.NetworkingModule
import pl.gov.mf.etoll.security.SecurityModule
import pl.gov.mf.etoll.starter.AppStarter
import pl.gov.mf.etoll.storage.StorageModule
import pl.gov.mf.etoll.ui.components.UIComponentsModule
import javax.inject.Inject


class NkspoApplicationImpl : NkspoApplication, MultiDexApplication() {

    @Inject
    lateinit var appStarter: AppStarter

    @Inject
    lateinit var timeShiftAdapter: TimeShiftAdapter

    private lateinit var _component: ApplicationComponentImpl
    val component: ApplicationComponentImpl
        get() {
            if (!::_component.isInitialized)
                _component =
                    DaggerApplicationComponentImpl.builder()
                        .applicationModule(ApplicationModule(this))
                        .commonsModule(CommonsModule())
                        .securityModule(SecurityModule())
                        .storageModule(StorageModule())
                        .networkingModule(NetworkingModule())
                        .coreModule(CoreModule())
                        .uIComponentsModule(UIComponentsModule())
                        .build()
            return _component
        }

    override fun getApplicationComponent(): ApplicationComponent = component

    override fun getForegroundActionIntent(): Intent {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return intent
    }

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()

        Log.d("NKSPO_APP",
            "STARTING APPLICATION! ${DateTime.now()}")
        component.inject(this)
        appStarter.start()
        component.demoWatchdog().start()
        component.loader().load().subscribe({
            component.useCaseLog().log(LogUseCase.APP, "App process started on device")
            timeShiftAdapter.onAppStartedAndLoaded()
        }, {

        })
    }
}
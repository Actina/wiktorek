package pl.gov.mf.etoll.appmode

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import javax.inject.Inject

class AppModeManagerImpl @Inject constructor(private val context: Context) : AppModeManager {

    companion object {
        private const val APP_MODE_FILENAME = "app_mode_settings"
        private const val APP_MODE_VAR_NAME = "defAppMode"
    }

    private val currentAppMode = BehaviorSubject.create<AppModeWrapper>()

    private var _sharedPreferences: SharedPreferences? = null
    private val sharedPreferences: SharedPreferences
        get() {
            if (_sharedPreferences == null)
                _sharedPreferences = context.getSharedPreferences(APP_MODE_FILENAME, MODE_PRIVATE)
            return _sharedPreferences!!
        }

    override fun setAppMode(appMode: AppMode, followSystem: Boolean): Completable =
        Completable.create {
            sharedPreferences.edit()
                .putInt(APP_MODE_VAR_NAME, if (followSystem) -1 else appMode.ordinal)
                .apply()
            currentAppMode.onNext(AppModeWrapper((if (followSystem) null else appMode)))
//            if (followSystem) {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//            } else if (appMode == AppMode.LIGHT_MODE)
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            else
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            it.onComplete()
        }

    override fun getCurrentAppMode(): AppMode = if (currentAppMode.hasValue()) {
        currentAppMode.value!!.mode ?: getSystemValue()
    } else {
        loadSynchronous()
        if (currentAppMode.hasValue())
            currentAppMode.value!!.mode ?: getSystemValue()
        else
            getSystemValue()
    }

    override fun getCurrentAppConfMode(): AppMode? = if (currentAppMode.hasValue()) {
        currentAppMode.value!!.mode
    } else {
        loadSynchronous()
        if (currentAppMode.hasValue())
            currentAppMode.value!!.mode
        else
            null
    }

    private fun getSystemValue(): AppMode {
        val nightModeFlags: Int = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) AppMode.DARK_MODE
        else AppMode.LIGHT_MODE
    }

    private fun loadSynchronous() {
        val initialMode = -1
        if (!sharedPreferences.contains(APP_MODE_VAR_NAME)) {
            // write default
            sharedPreferences.edit()
                .putInt(APP_MODE_VAR_NAME, -1)
                .apply()
            currentAppMode.onNext(AppModeWrapper(null))
        } else {
            // load value
            val modeNum = sharedPreferences.getInt(
                APP_MODE_VAR_NAME,
                0
            )
            val mode = if (modeNum < 0) null else AppMode.values()[modeNum]
            currentAppMode.onNext(
                AppModeWrapper(
                    mode
                )
            )
        }
    }

    override fun observeModeChanges(): Observable<AppMode> = currentAppMode.map {
        it.mode ?: getSystemValue()
    }

    override fun load(): Single<Boolean> = Single.create { emitter ->
        loadSynchronous()
        emitter.onSuccess(true)
    }

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        // nothing to do
    }

    inner class AppModeWrapper(val mode: AppMode?)
}

enum class AppMode {
    DARK_MODE,
    LIGHT_MODE
}
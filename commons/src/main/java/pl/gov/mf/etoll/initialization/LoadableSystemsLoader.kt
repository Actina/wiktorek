package pl.gov.mf.etoll.initialization

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import pl.gov.mf.etoll.commons.BuildConfig
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class LoadableSystemsLoader : LoadableSystem {
    private val systemsList = mutableListOf<LoadableSystem>()
    private var alreadyLoaded = AtomicBoolean(false)
    private var loading = AtomicBoolean(false)

    override fun load(): Single<Boolean> =
        Observable.interval(1, 3, TimeUnit.SECONDS).firstOrError().map {
            while (loading.get())
                Thread.sleep(10)
            if (!alreadyLoaded.get()) {
                loading.set(true)
                loadSequentially(true)
                loading.set(false)
            }
            alreadyLoaded.get()
        }.doOnError {
            loading.set(false)
            if (BuildConfig.FLAVOR.uppercase().contains("DEV"))
                throw RuntimeException(it)
            else
                Log.e("ERROR", it.localizedMessage)
        }.observeOn(AndroidSchedulers.mainThread())

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        // do nothing
    }

    fun loadSequentially(ignoreLoadingFlag: Boolean = false) {
        var output = true
        if (!alreadyLoaded.get() && (ignoreLoadingFlag || !loading.get())) {
            loading.set(true)
            for (i in systemsList.indices) {
                Log.d("Loading", systemsList[i].javaClass.name)
                output = output && systemsList[i].load().observeOn(AndroidSchedulers.mainThread()).subscribeOn(AndroidSchedulers.mainThread()).blockingGet()
            }
            alreadyLoaded.set(output)
            loading.set(false)
        } else while (loading.get())
            Thread.sleep(10)
    }

    fun chain(nextSystem: LoadableSystem): LoadableSystemsLoader =
        apply {
            systemsList.add(nextSystem)
            nextSystem.setOwner(this)
        }

}
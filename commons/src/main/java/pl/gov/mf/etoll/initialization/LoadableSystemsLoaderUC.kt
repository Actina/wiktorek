package pl.gov.mf.etoll.initialization

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

sealed class LoadableSystemsLoaderUC {
    companion object {
        // as loading is done in UI, we need this flag to avoid second loading in case of "don't preserve activities"
        private var systemsLoaded = false
    }

    class LoadSystemUseCase(private val ds: LoadableSystemsLoader) : LoadableSystemsLoaderUC() {
        fun execute(): Single<Boolean> = if (!systemsLoaded)
            ds.load()
                .map {
                    if (it) {
                        systemsLoaded = true
                    }
                    return@map it
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) else Single.just(true)
    }
}
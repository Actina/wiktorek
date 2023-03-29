package pl.gov.mf.etoll.initialization

import io.reactivex.Single

interface LoadableSystem {

    fun load(): Single<Boolean>
    fun setOwner(loadableSystemsLoader: LoadableSystemsLoader)
}
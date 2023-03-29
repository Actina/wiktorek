package pl.gov.mf.mobile.utils

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable?.disposeSafe() {
    this?.dispose()
}

fun CompositeDisposable?.disposeSafe() {
    this?.dispose()
}

fun CompositeDisposable?.addSafe(disposable: Disposable) {
    this?.add(disposable)
}
package pl.gov.mf.etoll.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * Removes old livedata observers and adds new one.
 * WARNING: DO NOT CONFUSE WITH "SINGLE EVENTS" OBSERVER.
 * To use e.g. when we don't want to create redundant observers in fragment's onResume
 * but we want to observe livedata there
 */
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    removeObservers(owner)
    observe(owner, observer)
}
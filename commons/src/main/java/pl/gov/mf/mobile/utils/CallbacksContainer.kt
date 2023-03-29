package pl.gov.mf.mobile.utils

import java.util.concurrent.ConcurrentHashMap

class CallbacksContainer<CALLBACK> {
    private val container: ConcurrentHashMap<String, CALLBACK> = ConcurrentHashMap()
    fun get(): Map<String, CALLBACK> = container

    fun set(callback: CALLBACK?, tag: String) {
        if (container.containsKey(tag))
            container.remove(tag)
        if (callback != null)
            container.put(tag, callback)
    }
}
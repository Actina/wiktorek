package pl.gov.mf.etoll.front.tecs.transaction.cache

class TecsCache {
    private var cache: Map<String, String>? = null
    fun updateCache(value: Map<String, String>?) {
        this.cache = value
    }

    fun getCache() = cache
}
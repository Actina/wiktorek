package pl.gov.mf.mobile.utils

import com.google.gson.Gson

interface JsonConvertible {
    fun toJSON(): String = Gson().toJson(this)
}

inline fun <reified T : JsonConvertible> String.toObject(): T {
    // TODO: comments left - uncomment, generate signed version and pass to testers to do full retention update tests
//    try {
    return Gson().fromJson(this, T::class.java)
//    } catch (ex: Exception) {
//        throw RuntimeException(ex.cause)
//    }
}

inline fun <reified T : JsonConvertible> T?.safeDeepCopy(): T? =
    try {
        if (this == null) null else Gson().fromJson(this.toJSON(), T::class.java)
    } catch (ex: Exception) {
        null
    }

inline fun <reified T : JsonConvertible> T.deepCopy(): T =
    Gson().fromJson(this.toJSON(), T::class.java)
package pl.gov.mf.etoll.networking.manager.eventstream

import com.google.gson.Gson

interface EventStreamGson {
    fun getGson(): Gson
}
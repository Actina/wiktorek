package pl.gov.mf.etoll.storage.database.logging

import io.reactivex.Completable
import io.reactivex.Single
import pl.gov.mf.etoll.logging.LoggingHelper
import pl.gov.mf.etoll.storage.database.logging.model.LoggingModel

interface LoggingDatabase : LoggingHelper {
    fun getNextChunk(): Single<List<LoggingModel>>
    fun removeOldest(count: Int): Completable
    fun add(item: LoggingModel): Completable

    companion object {
        const val CHUNK_SIZE = 100L
    }
}
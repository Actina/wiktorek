package pl.gov.mf.etoll.front.tecs.transaction.counter

import io.reactivex.Observable

interface TecsCounter {
    /**
     * Reset mechanics
     */
    fun initializeTransaction(id: Long)

    /**
     * Get info about left time
     */
    fun observeStatus(): Observable<Long>

}
package pl.gov.mf.etoll.core.crmmessages

import pl.gov.mf.etoll.core.model.CoreMessage

interface CrmMessagesManager {

    /**
     * This should be called whenever status has been updated - even if list is empty
     * This method also sends confirmation for all pending messages
     */
    fun onMessageIdsQueued(ids: List<String>)

    /**
     * Sent or save message seen confirmation
     */
    fun confirmMessageWasSeen(id: String)

    fun setCallbacks(callbacks: Callbacks, tag: String)

    fun removeCallbacks(tag: String)

    interface Callbacks {
        fun onNewMessageToShow(message: CoreMessage)
    }

    fun intervalCheck()
}
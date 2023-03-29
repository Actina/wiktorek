package pl.gov.mf.etoll.core.crmmessages

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import pl.gov.mf.etoll.core.CoreComposedUC
import pl.gov.mf.etoll.core.NetworkManagerUC
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import pl.gov.mf.mobile.networking.api.interceptors.WrongSystemTimeException
import pl.gov.mf.mobile.utils.CallbacksContainer
import pl.gov.mf.mobile.utils.addSafe
import pl.gov.mf.mobile.utils.toObject
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class CrmMessagesManagerImpl @Inject constructor(
    private val readSettingsUseCase: SettingsUC.ReadSettingsUseCase,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase,
    private val confirmMessagesUseCase: NetworkManagerUC.ConfirmMessagesUseCase,
    private val getMessagesUseCase: NetworkManagerUC.GetMessagesUseCase,
    private val timeIssuesDetectedUseCase: CoreComposedUC.TimeIssuesDetectedUseCase
) :
    CrmMessagesManager {

    private var callbacks: CallbacksContainer<CrmMessagesManager.Callbacks> = CallbacksContainer()
    private val compositeDisposable = CompositeDisposable()

    private lateinit var storage: CrmMessagesStore
    private val loaded
        get() = ::storage.isInitialized

    private val updateLock = AtomicBoolean(false)

    override fun onMessageIdsQueued(ids: List<String>) {
        if (updateLock.getAndSet(true)) return
        // load if needed
        if (!loaded) load()
        // clear from incoming list pending confirmations to avoid showing them again, also remove items for which we already know details
        val newIds = mutableListOf<String>()
        ids.forEach { id ->
            if (!storage.seenElements.map { it.messageId }
                    .contains(id) && !storage.newElements.map { it.messageId }
                    .contains(id))
                newIds.add(id)
        }

        // now request details for pending items
        if (newIds.isEmpty()) {
            if (storage.newElements.isNotEmpty()) {
                checkAndSendNextMessageToCallbacks()
            } else {
                // try to send pending confirmations
                if (storage.seenElements.isNotEmpty() && !confirmationLock.get()) {
                    confirmationLock.set(true)
                    sendConfirmations()
                }
            }
            updateLock.set(false)
            return
        }
        compositeDisposable.add(
            getMessagesUseCase.execute(newIds.toTypedArray())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    // filter just messages that interest us
                    val outputMessages = it.filter { newIds.contains(it.messageId) }
                    // and now add those messages to queue
                    if (outputMessages.isEmpty()) return@subscribe
                    storage.newElements.addAll(outputMessages)
                    // and save output messages
                    storeOutputMessages()
                    // and try to send info about new one to UI
                    checkAndSendNextMessageToCallbacks()
                    updateLock.set(false)
                }, {
                    if (it is WrongSystemTimeException) {
                        timeIssuesDetectedUseCase.execute()
                    }
                    // do nothing
                    Log.d("ERROR", it.localizedMessage)
                    updateLock.set(false)
                })
        )
    }

    private val confirmationLock = AtomicBoolean(false)

    override fun confirmMessageWasSeen(id: String) {
        // if we're locked, don't do anything
        if (confirmationLock.get()) return
        // now set lock
        confirmationLock.set(true)
        // update local list and save it to disc
        val element = storage.newElements.find { it.messageId.contentEquals(id) }
        element?.let {
            storage.seenElements.add(it)
            var foundPosition = -1
            for (i in storage.newElements.indices)
                if (storage.newElements[i].messageId.contentEquals(it.messageId))
                    foundPosition = i
            if (foundPosition >= 0)
                storage.newElements.removeAt(foundPosition)
        }
        // save info to disc
        storeOutputMessages()

        if (storage.seenElements.isNotEmpty()) {
            // try to send it
            sendConfirmations()
        } else {
            // just unlock, nothing to send
            confirmationLock.set(false)
        }
    }

    private fun sendConfirmations() {
        compositeDisposable.addSafe(confirmMessagesUseCase.execute(storage.seenElements.map { it.messageId }
            .toTypedArray()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            // confirmation was send correctly to server, so clean cache
            storage.seenElements.clear()
            storeOutputMessages()
            // unlock
            confirmationLock.set(false)
        }, {
            if (it is WrongSystemTimeException) {
                timeIssuesDetectedUseCase.execute()
            }
            // in case of error, just unlock lock
            confirmationLock.set(false)
        }))
    }

    override fun setCallbacks(callbacks: CrmMessagesManager.Callbacks, tag: String) {
        this.callbacks.set(callbacks, tag)
    }

    override fun intervalCheck() {
        checkAndSendNextMessageToCallbacks()
    }

    private fun checkAndSendNextMessageToCallbacks() {
        // we avoid doing that too fast, ie. on splash, so let's first check if we're loaded
        if (!loaded) return
        if (updateLock.get()) return
        if (storage.newElements.isEmpty()) return
        callbacks.get().forEach { it.value.onNewMessageToShow(storage.newElements[0]) }
    }

    override fun removeCallbacks(tag: String) {
        this.callbacks.set(null, tag)
    }

    private fun load() {
        if (loaded) return
        var readData = CrmMessagesStore(mutableListOf(), mutableListOf())
        if (readSettingsUseCase.executeForString(Settings.CRM_MESSAGE_QUEUE).isNotEmpty())
            readData = readSettingsUseCase.executeForString(Settings.CRM_MESSAGE_QUEUE).toObject()
        storage = readData
    }

    private fun storeOutputMessages() {
        // write and do not wait for callback
        compositeDisposable.addSafe(
            writeSettingsUseCase.execute(
                Settings.CRM_MESSAGE_QUEUE,
                storage.toJSON()
            ).subscribe()
        )
    }
}
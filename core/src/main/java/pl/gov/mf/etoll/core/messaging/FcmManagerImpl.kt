package pl.gov.mf.etoll.core.messaging

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Single
import pl.gov.mf.etoll.initialization.LoadableSystemsLoader
import pl.gov.mf.etoll.storage.settings.Settings
import pl.gov.mf.etoll.storage.settings.SettingsUC
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class FcmManagerImpl @Inject constructor(
    private val context: Context,
    private val writeSettingsUseCase: SettingsUC.WriteSettingsUseCase
) :
    FcmManager {

    private var isGoogleServiceAvailable: Boolean = false
    private var isLoaded = AtomicBoolean(false)

    // load system, check if google services are available and if not - continue, without
    // firebase registration; in case of any firebase issues - continue
    override fun load(): Single<Boolean> =
        Single.create { emitter ->
            if (isLoaded.getAndSet(true)) {
                emitter.onSuccess(true)
                return@create
            }
            isGoogleServiceAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS


            if (isGoogleServiceAvailable) {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            emitter.onSuccess(true)
                            return@OnCompleteListener
                        }
                        val token = task.result
                        if (token != null)
                        writeSettingsUseCase.execute(Settings.FIREBASE_ID, token)
                            .subscribe {}
                    })
            }
            emitter.onSuccess(true)
        }

    override fun setOwner(loadableSystemsLoader: LoadableSystemsLoader) {
        // do nothing
    }
}
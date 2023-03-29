package pl.gov.mf.etoll.overlay

import android.app.Service
import android.content.Intent
import android.os.IBinder
import pl.gov.mf.etoll.app.NkspoApplicationImpl
import javax.inject.Inject


class OverlayService : Service() {

    @Inject
    lateinit var servicePresenter: OverlayServiceContract.Presenter

    private val serviceView: OverlayServiceContract.View = OverlayServiceView()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (applicationContext as NkspoApplicationImpl).component.inject(this)
        servicePresenter.takeView(serviceView, applicationContext)
        if (intent == null) {
            return START_NOT_STICKY
        }

        return super.onStartCommand(intent, flags, START_STICKY)
    }

    override fun onDestroy() {
        super.onDestroy()
        servicePresenter.dropView()
    }
}
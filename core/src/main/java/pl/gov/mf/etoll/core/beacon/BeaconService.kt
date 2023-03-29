package pl.gov.mf.etoll.core.beacon

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.mf.etoll.core.app.NkspoApplication
import pl.gov.mf.mobile.utils.LocationWrapper
import pl.gov.mf.mobile.utils.wrap
import java.util.*

class BeaconService : Service() {

    var FOLDER_PATH: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path.toString() + "/test/"

    private val CHANNEL_ID = "ServiceApp"
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val locationWrapper = location.wrap().normalize()
            val dt = Date(locationWrapper.time)

            Log.d("Loc", "${locationWrapper.longitude} ${locationWrapper.latitude} time $dt")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderDisabled(provider: String) = Unit
        override fun onProviderEnabled(provider: String) = Unit
    }

    companion object {
        var isMyServiceRunning = false
        var restartShouldBeEnabled = false

        fun startService(context: Context) {
            restartShouldBeEnabled = true
            if (!isMyServiceRunning) {
                val startIntent = Intent(context, BeaconService::class.java)
                ContextCompat.startForegroundService(context, startIntent)
                isMyServiceRunning = true
            }
        }

        fun stopService(context: Context) {
            restartShouldBeEnabled = false
            val stopIntent = Intent(context, BeaconService::class.java)
            context.stopService(stopIntent)
            isMyServiceRunning = false
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        val rideCoordinatorConfiguration = (application as NkspoApplication)
            .getApplicationComponent().useCaseGetRideCoordinatorConfiguration()
        val isMonitoringByApp =
            rideCoordinatorConfiguration.execute()?.trackByApp ?: false
        if (!isMonitoringByApp)
            return

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val handler = Handler()
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener
        )


        handler.postDelayed(object : Runnable {
            @SuppressLint("MissingPermission")
            override fun run() {

                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.also { location ->
                    val locationWrapper = location.wrap().normalize()
                    val dt = Date(locationWrapper.time)
                    val txt = " time $dt latitude: ${locationWrapper.latitude} longitude: ${locationWrapper.longitude}\n"
                    Log.e("location", txt)
                    saveLocation(locationWrapper)
                }
                handler.postDelayed(this, 1000)//1 sec delay
            }
        }, 0)
    }


    @Synchronized
    private fun saveLocation(location: LocationWrapper) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
//                AppDatabase.getInstance(this@BeaconService)
//                    .locationDao()
//                    .insert(
//                        Location(
//                            location = "${location.longitude} ${location.latitude} ", timestamp = location.time
//                        )
//                    )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationManager =
            (application as NkspoApplication).getApplicationComponent().notificationManager()
        val notificationIntent = notificationManager.createForegroundServiceNotification(
            emptyList(),
            (application as NkspoApplication).getForegroundActionIntent()
        )

        startForeground(
            pl.gov.mf.etoll.core.notifications.NotificationManager.FOREGROUND_NOTIFICATION_ID,
            notificationIntent
        )

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (restartShouldBeEnabled) {
            val broadcastIntent = Intent()
            broadcastIntent.action = "restartservice"
            broadcastIntent.setClass(this, Restarter::class.java)
            this.sendBroadcast(broadcastIntent)
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }


}
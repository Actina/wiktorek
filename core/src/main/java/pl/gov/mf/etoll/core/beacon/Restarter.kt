package pl.gov.mf.etoll.core.beacon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class Restarter : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, BeaconService::class.java))
        } else {
            context.startService(Intent(context, BeaconService::class.java))
        }
    }
}
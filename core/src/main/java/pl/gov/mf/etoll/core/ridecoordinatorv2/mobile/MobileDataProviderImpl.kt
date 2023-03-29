package pl.gov.mf.etoll.core.ridecoordinatorv2.mobile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.*
import androidx.core.app.ActivityCompat
import javax.inject.Inject

class MobileDataProviderImpl @Inject constructor(private val context: Context) :
    MobileDataprovider {

    private var status: MobileDataproviderStatus = MobileDataproviderStatus.WORKING

    override fun getStatus(): MobileDataproviderStatus = status

    override fun getLatestData(): MobileData? {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            status = MobileDataproviderStatus.PERMISSION_ISSUES
            return null
        } else {
            val telManager = context.getSystemService(Context.TELEPHONY_SERVICE)
            if (telManager != null && telManager is TelephonyManager && telManager.allCellInfo != null) {
                when (val cellInfo = telManager.allCellInfo.selectBest()) {
                    is CellInfoGsm -> {
                        val identityGsm = (cellInfo).cellIdentity
                        return identityGsm.toRideDataFeederMobileData()
                    }
                    is CellInfoCdma -> {
                        val identityCdma = cellInfo.cellIdentity
                        return identityCdma.toRideDataFeederMobileData()
                    }
                    is CellInfoLte -> {
                        val identityLte = cellInfo.cellIdentity
                        return identityLte.toRideDataFeederMobileData()
                    }
                    is CellInfoWcdma -> {
                        val identityCdma = cellInfo.cellIdentity
                        return identityCdma.toRideDataFeederMobileData()
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
        return null
    }
}
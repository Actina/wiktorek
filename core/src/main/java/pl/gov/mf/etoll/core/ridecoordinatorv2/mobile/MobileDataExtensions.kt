package pl.gov.mf.etoll.core.ridecoordinatorv2.mobile

import android.telephony.*


fun CellIdentityGsm.toRideDataFeederMobileData(): MobileData =
    MobileData(
        lac.toString(),
        formatMcc(mcc),
        formatMnc(mnc),
        formatCid(cid.toString())
    )

fun CellIdentityCdma.toRideDataFeederMobileData(): MobileData =
    MobileData(
        cid = formatCid(basestationId.toString())
    )

fun CellIdentityLte.toRideDataFeederMobileData(): MobileData =
    MobileData(
        tac.toString(),
        formatMcc(mcc),
        formatMnc(mnc),
        formatCid(ci.toString())
    )

fun CellIdentityWcdma.toRideDataFeederMobileData(): MobileData =
    MobileData(
        lac.toString(),
        formatMcc(mcc),
        formatMnc(mnc),
        formatCid(cid.toString())
    )

fun formatMcc(mcc: Int): String? =
    if (mcc < 10)
        "00$mcc"
    else if (mcc < 100)
        "0$mcc"
    else "$mcc"

fun formatMnc(mnc: Int): String? =
    if (mnc < 10)
        "0$mnc"
    else
        "$mnc"

fun formatCid(cid: String?): String? {
    if (cid == null) return null
    var outCid = cid
    while (outCid!!.length < 9) outCid = "0$outCid"
    return outCid
}

fun List<CellInfo>.selectBest(function: (CellInfo) -> Unit) {
    var suggestedOutput: CellInfo? = null
    for (info in this) {
        when (info) {
            is CellInfoCdma -> {
                if (info.cellIdentity.basestationId < Integer.MAX_VALUE) {
                    suggestedOutput = info
                }
            }
            is CellInfoGsm -> {
                if (info.cellIdentity.lac < Integer.MAX_VALUE) {
                    function(info)
                    return
                }
            }
            is CellInfoLte -> {
                if (info.cellIdentity.mnc < Integer.MAX_VALUE) {
                    function(info)
                    return
                }
            }
            is CellInfoWcdma -> {
                if (info.cellIdentity.lac < Integer.MAX_VALUE) {
                    suggestedOutput = info
                }
            }
        }
    }
    suggestedOutput?.let(function)
    return
}

fun List<CellInfo>.selectBest(): CellInfo? {
    var suggestedOutput: CellInfo? = null
    for (info in this) {
        when (info) {
            is CellInfoCdma -> {
                if (info.cellIdentity.basestationId < Integer.MAX_VALUE) {
                    suggestedOutput = info
                }
            }
            is CellInfoGsm -> {
                if (info.cellIdentity.lac < Integer.MAX_VALUE) {
                    return info
                }
            }
            is CellInfoLte -> {
                if (info.cellIdentity.mnc < Integer.MAX_VALUE) {
                    return info
                }
            }
            is CellInfoWcdma -> {
                if (info.cellIdentity.lac < Integer.MAX_VALUE) {
                    suggestedOutput = info
                }
            }
        }
    }
    return suggestedOutput
}
package pl.gov.mf.etoll.front.configtrailercategory

import pl.gov.mf.mobile.utils.JsonConvertible

data class ConfigTrailerData(
    var entryScreenId: Int = -1,
    var weightExceeded: Boolean = false,
    var licensePlateNumber: String = "",
    var countryCode: String = ""
) : JsonConvertible

package pl.gov.mf.etoll.front.errors.gpsissues

import pl.gov.mf.mobile.utils.JsonConvertible

data class GpsIssuesModel(val issues: List<Int>) : JsonConvertible
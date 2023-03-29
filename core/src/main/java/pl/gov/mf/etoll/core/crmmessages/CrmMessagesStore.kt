package pl.gov.mf.etoll.core.crmmessages

import com.google.gson.annotations.SerializedName
import pl.gov.mf.etoll.core.model.CoreMessage
import pl.gov.mf.mobile.utils.JsonConvertible

data class CrmMessagesStore(
    @SerializedName("storageSeen") var seenElements: MutableList<CoreMessage> = mutableListOf(),
    @SerializedName("storage") var newElements: MutableList<CoreMessage> = mutableListOf()
) : JsonConvertible
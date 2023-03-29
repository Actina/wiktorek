package pl.gov.mf.etoll.networking.api.model

import com.google.gson.annotations.SerializedName

data class ApiConfirmMessagesRequest(
    @SerializedName("messageIds") var messageIds: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApiConfirmMessagesRequest

        if (!messageIds.contentEquals(other.messageIds)) return false

        return true
    }

    override fun hashCode(): Int {
        return messageIds.contentHashCode()
    }
}
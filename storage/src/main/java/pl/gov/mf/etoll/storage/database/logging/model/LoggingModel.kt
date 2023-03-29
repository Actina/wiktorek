package pl.gov.mf.etoll.storage.database.logging.model

data class LoggingModel(
    val id: Long? = -1,
    val date: String,
    val tag: String,
    val description: String
)
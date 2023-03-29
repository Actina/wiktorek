package pl.gov.mf.etoll.core.devicecompatibility.types

data class SystemInfoData(
    val systemVersionNumber: String,
    val apiLevel: String,
    val apiCodeName: String,
    val playServicesVersion: String
)
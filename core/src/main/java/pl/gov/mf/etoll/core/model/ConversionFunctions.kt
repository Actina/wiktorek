package pl.gov.mf.etoll.core.model

import pl.gov.mf.etoll.networking.api.model.*


fun ApiLastPositionsSentResponse.toCoreModel(): CoreLastPositionsSent = CoreLastPositionsSent(
    applicationLastPosition.toCoreModel(),
    zslLastPosition?.toCoreModel()
)

fun ZslLastPosition.toCoreModel(): CoreZslLastPosition =
    CoreZslLastPosition(zslBussinessId, longitude, latitude, dateTimestamp)

fun ApplicationLastPosition.toCoreModel(): CoreApplicationLastPosition =
    CoreApplicationLastPosition(applicationId, longitude, latitude, dateTimestamp)

fun ApiStatusResponse.toCoreModel(): CoreStatus {
    val outVehicles = mutableListOf<CoreVehicle>()
    this.vehicles.forEach {
        outVehicles.add(it.toCoreModel())
    }
    return CoreStatus(
        applicationId, dateTimestamp, outVehicles.toTypedArray(), transit.toCoreModel(),
        messageIds, sentActivated, crmActivated, eventStream.toCoreModel()
    )
}

private fun ApiEventStreamConfiguration.toCoreModel(): CoreEventStreamConfiguration =
    CoreEventStreamConfiguration(address, authorizationHeader)

fun ApiModelVehicle.toCoreModel(): CoreVehicle =
    CoreVehicle(
        id,
        licensePlate,
        brand,
        model,
        emissionClass,
        category,
        categoryCanBeIncreased,
        zslIsPrimaryGeolocator,
        categoryPlateVerification,
        accountBalance.toCoreModel(),
        geolocator?.toCoreModel(),
        lowTopUpFlag = lowTopUpFlag,
        tollCategoryIncreaseFlag = tollCategoryIncreaseFlag
    )

fun ApiModelAccountInfo.toCoreModel(): CoreAccountInfo =
    CoreAccountInfo(id, alias, type, balance.toCoreModel())

fun BalanceInfo?.toCoreModel(): CoreBalance? =
    if (this == null) null else CoreBalance(priority, updateAtTimestamp, value, isInitialized)

fun Transit.toCoreModel(): CoreConfiguration =
    CoreConfiguration(
        samplingRate,
        collectionRate,
        sentSamplingRate,
        sentCollectionRate,
        sentAlgorithmUsed
    )

fun ApiModelGeolocator.toCoreModel(): CoreGeolocator = CoreGeolocator(number, obeType)

fun Map<String, List<ApiSentItem>>.toCoreModel(): CoreSentMap {
    if (isEmpty()) return CoreSentMap(emptyMap())
    val coreSentMap = hashMapOf<String, MutableList<CoreSent>>()
    forEach { mapElement ->
        val coreSent = mapElement.value.map(ApiSentItem::toCoreModel).toMutableList()
        coreSentMap[mapElement.key] = coreSent
    }
    return CoreSentMap(coreSentMap)
}

fun List<ApiServerMessage>.toCoreModel(): List<CoreMessage> {
    val output = mutableListOf<CoreMessage>()
    forEach {
        output.add(
            CoreMessage(
                it.messageId,
                it.contents,
                it.headers,
                it.sendDateTimestamp,
                it.type
            )
        )
    }
    return output
}
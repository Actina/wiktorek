package pl.gov.mf.etoll.networking.manager.eventstream.apibuilder

import pl.gov.mf.etoll.networking.api.EventStreamAPI
import pl.gov.mf.etoll.networking.api.model.ApiEventStreamConfiguration

interface EventStreamApiBuilder {
    fun buildApiFrom(configuration: ApiEventStreamConfiguration): EventStreamAPI
}
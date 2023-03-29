package pl.gov.mf.etoll.core.ridecoordinatorv2.filter

import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.collection.*
import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
import javax.inject.Inject

class RideDataFilterDecorator @Inject constructor(
    rule1: GpsRuleBE06,
    rule2: GpsRuleBN06,
    rule3: GpsRuleBS06,
    rule4: GpsRuleBW06,
    rule5: GpsRuleLESEUA,
    rule6: GpsRuleLSSWCZ,
    rule7: GpsRuleSNERU
) : RideDataFilter {

    private val filters = mutableListOf<RideDataFilter>()

    init {
        chain(rule1)
        chain(rule2)
        chain(rule3)
        chain(rule4)
        chain(rule5)
        chain(rule6)
        chain(rule7)
    }


    /**
     * Add next filter to chain
     */
    fun chain(filter: RideDataFilter): RideDataFilterDecorator = apply {
        filters.add(filter)
    }

    override fun isDataValid(data: EventStreamLocation): Boolean {
        // if at least one filter is not passing, data is invalid
        filters.forEach { filter ->
            if (!filter.isDataValid(data))
                return false
        }
        return true
    }
}
package com.billennium.core.ridecoordinator

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import pl.gov.mf.mobile.utils.checkSum

class RideCoordinatorUCTest {

    @Test
    fun validateSerialNumber() {
        val sampleFullSerialNumber =
            "53454350484f4e455f3030303030303030303030303030303031383835343430_M20-BY55HC-6"
        val serialNumberCheckPattern = Regex("^[a-zA-Z0-9\\-_]{1,50}$")
        assertFalse(serialNumberCheckPattern.containsMatchIn(sampleFullSerialNumber))

        val shortSerialNumber = sampleFullSerialNumber.checkSum()
        assertTrue(serialNumberCheckPattern.containsMatchIn(shortSerialNumber))
        println(shortSerialNumber)
    }
}
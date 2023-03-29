package com.billennium.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import pl.gov.mf.mobile.security.deviceId.DeviceIdProviderImpl
import pl.gov.mf.mobile.security.deviceId.generators.IdGenerators.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class DeviceIdProviderImplTest {

    private lateinit var deviceIdProviderImpl: DeviceIdProviderImpl

    @Before
    fun setUp() {
        deviceIdProviderImpl = DeviceIdProviderImpl(
            MediaDrmDeviceIdGenerator(),
            SecureIdDeviceIdGenerator(InstrumentationRegistry.getInstrumentation().targetContext.contentResolver),
            BuildSerialDeviceIdGenerator(),
            PseudoUniqueDeviceIdGenerator()
        )
    }

    @Test
    fun getDeviceId_idsTheSameEveryTime() {
        val deviceIdA = deviceIdProviderImpl.getDeviceId()
        val deviceIdB = deviceIdProviderImpl.getDeviceId()
        assertFalse(deviceIdA.isEmpty())
        assertFalse(deviceIdB.isEmpty())
        assertEquals(deviceIdA, deviceIdB)
    }

}
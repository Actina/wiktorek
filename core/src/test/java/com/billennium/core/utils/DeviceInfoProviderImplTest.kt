package com.billennium.core.utils

import android.content.Context
import android.os.PowerManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import pl.gov.mf.etoll.core.devicecompatibility.DeviceInfoProvider
import pl.gov.mf.etoll.core.devicecompatibility.DeviceInfoProviderImpl
import pl.gov.mf.etoll.core.devicecompatibility.types.BatteryOptimisationInfo.*
import pl.gov.mf.etoll.networking.NetworkingUC.CheckRemoteInternetConnectionUseCase

class DeviceInfoProviderImplTest {

    private lateinit var deviceInfoProviderSpyK: DeviceInfoProvider

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        val mockedContext = mockk<Context>()
        every { mockedContext.packageName } returns "com.billennium.nkspo"

        deviceInfoProviderSpyK =
            spyk(
                DeviceInfoProviderImpl(
                    mockedContext,
                    CheckRemoteInternetConnectionUseCase()
                )
            )
    }

    @Test
    fun noPowerManager_isBatteryOptimisationEnabled_noInfo() {
        every { deviceInfoProviderSpyK.getPowerManager() } returns null
        assertEquals(deviceInfoProviderSpyK.isBatteryOptimisationEnabled(), NO_INFO)
    }

    @Test
    fun ignoringOptimisation_isBatteryOptimisationEnabled_optimisationDisabled() {
        //Note: I can mock final classes. Its possible also in androidTest, but since Android 9.0
        val powerManager = mockk<PowerManager>()
        every { powerManager.isIgnoringBatteryOptimizations(any()) } returns true

        every { deviceInfoProviderSpyK.getPowerManager() } returns powerManager

        assertEquals(deviceInfoProviderSpyK.isBatteryOptimisationEnabled(), OPTIMISATION_DISABLED)
    }

    @Test
    fun notIgnoringOptimisation_isBatteryOptimisationEnabled_optimisationEnabled() {
        val powerManager = mockk<PowerManager>()
        every { powerManager.isIgnoringBatteryOptimizations(any()) } returns false

        every { deviceInfoProviderSpyK.getPowerManager() } returns powerManager

        assertEquals(deviceInfoProviderSpyK.isBatteryOptimisationEnabled(), OPTIMISATION_ENABLED)
    }

}
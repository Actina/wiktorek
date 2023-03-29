package pl.gov.mf.etoll.front.errors.gpsissues

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.gov.mf.etoll.core.ridecoordinatorv2.location.issues.LocationIssuesDetector

class GpsIssuesViewModelTest {
    @Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var testSubject: GpsIssuesViewModel

    @Before
    fun setUp() {
        testSubject = GpsIssuesViewModel()
    }

    @Test
    fun setModel_noData() {
        testSubject.setModel(GpsIssuesModel(emptyList()))
        assertTrue(testSubject.issuesCriticalLowBattery.value == false)
        assertTrue(testSubject.issuesCriticalLowBatteryButLoading.value == false)
        assertTrue(testSubject.issuesLowBattery.value == false)
        assertTrue(testSubject.issuesOverheat.value == false)
    }

    @Test
    fun setModel_criticalBatteryLoading() {
        testSubject.setModel(
            GpsIssuesModel(
                listOf(
                    LocationIssuesDetector.ISSUE_CRITICAL_BATTERY,
                    LocationIssuesDetector.ISSUE_CRITICAL_BATTERY_LOADING,
                    LocationIssuesDetector.ISSUE_LOW_BATTERY
                )
            )
        )
        assertTrue(testSubject.issuesCriticalLowBattery.value == false)
        assertTrue(testSubject.issuesCriticalLowBatteryButLoading.value == true)
        assertTrue(testSubject.issuesLowBattery.value == false)
        assertTrue(testSubject.issuesOverheat.value == false)
    }
}
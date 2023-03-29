//package pl.gov.mf.etoll.front.config
//
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Test
//import pl.gov.mf.etoll.core.model.*
//
//class RideConfigurationCoordinatorImplTest {
//    // in case of issues when launching this test, do this
//    // https://stackoverflow.com/a/50687472
//
//    private val vehicle_WithoutOBE_WithoutTrailer = CoreVehicle(
//        id = 1,
//        licensePlate = "BILLE",
//        brand = "Brand",
//        model = "Model",
//        emissionClass = "6e",
//        category = 13,
//        categoryCanBeIncreased = false,
//        zslIsPrimaryGeolocator = false,
//        categoryPlateVerification = false,
//        accountInfo = CoreAccountInfo(1, "account", "prepaid", null),
//        geolocator = null
//    )
//
//    private val vehicle_WithOBE_WithoutTrailer = CoreVehicle(
//        id = 1,
//        licensePlate = "BILLE",
//        brand = "Brand",
//        model = "Model",
//        emissionClass = "6e",
//        category = 13,
//        categoryCanBeIncreased = false,
//        zslIsPrimaryGeolocator = false,
//        categoryPlateVerification = false,
//        accountInfo = CoreAccountInfo(1, "account", "prepaid", null),
//        geolocator = CoreGeolocator("", "zsl")
//    )
//
//    private val vehicle_WithoutOBE_WithTrailer = CoreVehicle(
//        id = 1,
//        licensePlate = "BILLE",
//        brand = "Brand",
//        model = "Model",
//        emissionClass = "6e",
//        category = 14,
//        categoryCanBeIncreased = true,
//        zslIsPrimaryGeolocator = false,
//        categoryPlateVerification = false,
//        accountInfo = CoreAccountInfo(1, "account", "prepaid", null),
//        geolocator = null
//    )
//
//    private val vehicle_WithOBE_WithTrailer = CoreVehicle(
//        id = 1,
//        licensePlate = "BILLE",
//        brand = "Brand",
//        model = "Model",
//        emissionClass = "6e",
//        category = 14,
//        categoryCanBeIncreased = true,
//        zslIsPrimaryGeolocator = false,
//        categoryPlateVerification = false,
//        accountInfo = CoreAccountInfo(1, "account", "prepaid", null),
//        geolocator = CoreGeolocator("", "zsl")
//    )
//
//    private val defaultConfiguration = CoreConfiguration(60, 5)
//    private val defaultEventStreamConfiguration = CoreEventStreamConfiguration("", "")
//
//
//    @Before
//    fun setUp() {
//    }
//
//    /**
//     * Case 1:
//     * - tylko przejazd platny jest mozliwy
//     * - jeden pojazd, kategorii 13
//     * - brak przyczepy
//     * - brak OBE
//     *
//     * Expected:
//     * - user po wyborze typu przejazdu, konczy konfiguracje
//     * - brak mozliwosci zmiany trybu monitorowania, brak mozliwosci zmiany przyczepy
//     * - automatycznie: monitorowanie przez aplikacje, jedyny pojazd
//     */
//    @Test
//    fun testCase01() {
//        val status = CoreStatus(
//            applicationId = "01",
//            dateTimestamp = 0,
//            vehicles = arrayOf(vehicle_WithoutOBE_WithoutTrailer),
//            configuration = defaultConfiguration,
//            messageIds = arrayOf(),
//            sentActivated = false,
//            crmActivated = true,
//            minimumSystemVersion = "",
//            eventStreamConfiguration = defaultEventStreamConfiguration
//        )
//
//        val coordinator: RideConfigurationCoordinator = RideConfigurationCoordinatorImpl()
//        // reset configuration
//        coordinator.resetCoordinator()
//        // initial configuration
//        coordinator.onConfigurationStarted(
//            sentIsPossible = false,
//            tolledIsPossible = true,
//            sentWasOffline = false,
//            sentList = null,
//            status = status
//        )
//        // ride selection screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION)
//        // select tolled
//        coordinator.onRideModeSelected(tolled = true, sent = false)
//        // path should lead to finish of configuration - nothing to select (no secondary geolocator)
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH,
//            coordinator.getNextStep()
//        )
//    }
//
//    /**
//     * Case 2:
//     * - tylko przejazd platny jest mozliwy
//     * - jeden pojazd, kategorii 13
//     * - brak przyczepy
//     * - jest OBE
//     *
//     * Expected:
//     * - user po wyborze typu przejazdu, zostaje przekierowany na wybor urzadzenia monitorujacego
//     * - user po wyborze urzadzenia monitorujacego konczy konfiguracje
//     * - mozliwosci zmiany trybu monitorowania, brak mozliwosci zmiany przyczepy
//     */
//    @Test
//    fun testCase02() {
//        val status = CoreStatus(
//            applicationId = "01",
//            dateTimestamp = 0,
//            vehicles = arrayOf(vehicle_WithOBE_WithoutTrailer),
//            configuration = defaultConfiguration,
//            messageIds = arrayOf(),
//            sentActivated = false,
//            crmActivated = true,
//            minimumSystemVersion = "",
//            eventStreamConfiguration = defaultEventStreamConfiguration
//        )
//
//        val coordinator: RideConfigurationCoordinator = RideConfigurationCoordinatorImpl()
//        // reset configuration
//        coordinator.resetCoordinator()
//        // initial configuration
//        coordinator.onConfigurationStarted(
//            sentIsPossible = false,
//            tolledIsPossible = true,
//            sentWasOffline = false,
//            sentList = null,
//            status = status
//        )
//        // ride selection screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION)
//        // select tolled
//        coordinator.onRideModeSelected(tolled = true, sent = false)
//        // path should lead to monitoring device selection
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE,
//            coordinator.getNextStep()
//        )
//        // next step - monitoring device
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE)
//        // select monitoring by app
//        coordinator.onMonitoringDeviceSelected(true)
//        // check next steps
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH,
//            coordinator.getNextStep()
//        )
//        // TODO: check output configuration
//    }
//
//    /**
//     * Case 3:
//     * - tylko przejazd platny jest mozliwy
//     * - dwa pojazdy, kategorii 13
//     * - brak przyczepy
//     * - brak obe
//     *
//     * Expected:
//     * - user po wyborze typu przejazdu, zostaje przekierowany na wybor pojazdu
//     * - user po wyborze pojazdu konczy konfiguracje
//     * - brak mozliwosci zmiany trybu monitorowania, brak mozliwosci zmiany przyczepy
//     * - automatycznie: monitorowanie przez aplikacje
//     */
//    @Test
//    fun testCase03() {
//        val status = CoreStatus(
//            applicationId = "01",
//            dateTimestamp = 0,
//            vehicles = arrayOf(
//                vehicle_WithoutOBE_WithoutTrailer,
//                vehicle_WithoutOBE_WithoutTrailer
//            ),
//            configuration = defaultConfiguration,
//            messageIds = arrayOf(),
//            sentActivated = false,
//            crmActivated = true,
//            minimumSystemVersion = "",
//            eventStreamConfiguration = defaultEventStreamConfiguration
//        )
//
//        val coordinator: RideConfigurationCoordinator = RideConfigurationCoordinatorImpl()
//        // reset configuration
//        coordinator.resetCoordinator()
//        // initial configuration
//        coordinator.onConfigurationStarted(
//            sentIsPossible = false,
//            tolledIsPossible = true,
//            sentWasOffline = false,
//            sentList = null,
//            status = status
//        )
//        // ride selection screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION)
//        // select tolled
//        coordinator.onRideModeSelected(tolled = true, sent = false)
//        // path should lead to finish of configuration - but monitoring mode change should be possible later in UI
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION,
//            coordinator.getNextStep()
//        )
//        // vehicle selection screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION)
//        // select vehicle
//        coordinator.onVehicleSelected(vehicle_WithoutOBE_WithoutTrailer)
//        // we should finish configuration at this step
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH,
//            coordinator.getNextStep()
//        )
//        // TODO: check output configuration
//    }
//
//
//    /**
//     * Case 4:
//     * - tylko przejazd platny jest mozliwy
//     * - dwa pojazdy, kategorii 13
//     * - brak przyczepy
//     * - oba z obe
//     *
//     * Expected:
//     * - user po wyborze typu przejazdu, zostaje przekierowany na wybor pojazdu
//     * - user po wyborze pojazdu zostaje przekierowany na wybor urzadzenia monitorujacego
//     * - user po wyborze urzadzenia monitorujacego konczy konfiguracje
//     * - mozliwosc zmiany trybu monitorowania, brak mozliwosci zmiany przyczepy
//     */
//    @Test
//    fun testCase04() {
//        val status = CoreStatus(
//            applicationId = "01",
//            dateTimestamp = 0,
//            vehicles = arrayOf(vehicle_WithOBE_WithoutTrailer, vehicle_WithOBE_WithoutTrailer),
//            configuration = defaultConfiguration,
//            messageIds = arrayOf(),
//            sentActivated = false,
//            crmActivated = true,
//            minimumSystemVersion = "",
//            eventStreamConfiguration = defaultEventStreamConfiguration
//        )
//
//        val coordinator: RideConfigurationCoordinator = RideConfigurationCoordinatorImpl()
//        // reset configuration
//        coordinator.resetCoordinator()
//        // initial configuration
//        coordinator.onConfigurationStarted(
//            sentIsPossible = false,
//            tolledIsPossible = true,
//            sentWasOffline = false,
//            sentList = null,
//            status = status
//        )
//        // ride selection screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION)
//        // select tolled
//        coordinator.onRideModeSelected(tolled = true, sent = false)
//        // path should lead to finish of configuration - but monitoring mode change should be possible later in UI
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION,
//            coordinator.getNextStep()
//        )
//        // vehicle selection screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.VEHICLE_SELECTION)
//        // select vehicle
//        coordinator.onVehicleSelected(vehicle_WithOBE_WithoutTrailer)
//        // we should go to monitoring device selection now
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE,
//            coordinator.getNextStep()
//        )
//        // monitoring device screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE)
//        // select monitoring by obe
//        coordinator.onMonitoringDeviceSelected(false)
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH,
//            coordinator.getNextStep()
//        )
//
//        // TODO: check output configuration
//    }
//
//    /**
//     * Case 5:
//     * - tylko przejazd platny jest mozliwy
//     * - jeden pojazd, kategorii 14 z mozliwoscia przekroczenia wagi
//     * - brak obe
//     *
//     * Expected:
//     * - user po wyborze typu przejazdu, zostaje przekierowany na informacje o przyczepie
//     * - user po akceptacji informacji o przyczepie, konczy konfiguracje
//     * - brak mozliwosci zmiany trybu monitorowania, mozliwosc zmiany przyczepy
//     * - automatycznie: pojazd
//     */
//    @Test
//    fun testCase05() {
//        val status = CoreStatus(
//            applicationId = "01",
//            dateTimestamp = 0,
//            vehicles = arrayOf(vehicle_WithoutOBE_WithTrailer),
//            configuration = defaultConfiguration,
//            messageIds = arrayOf(),
//            sentActivated = false,
//            crmActivated = true,
//            minimumSystemVersion = "",
//            eventStreamConfiguration = defaultEventStreamConfiguration
//        )
//
//        val coordinator: RideConfigurationCoordinator = RideConfigurationCoordinatorImpl()
//        // reset configuration
//        coordinator.resetCoordinator()
//        // initial configuration
//        coordinator.onConfigurationStarted(
//            sentIsPossible = false,
//            tolledIsPossible = true,
//            sentWasOffline = false,
//            sentList = null,
//            status = status
//        )
//        // ride selection screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION)
//        // select tolled
//        coordinator.onRideModeSelected(tolled = true, sent = false)
//        // check next step
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.TRAILER,
//            coordinator.getNextStep()
//        )
//        // show next step
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.TRAILER)
//        // declare ride with trailer exceeding weight
//        coordinator.onTrailerSelected(noTrailer = false, increaseFlagDeclared = true)
//        // we should finish configuration now
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH,
//            coordinator.getNextStep()
//        )
//        // TODO: check output configuration
//    }
//
//    /**
//     * Case 6:
//     * - tylko przejazd platny jest mozliwy
//     * - jeden pojazd, kategorii 14 z mozliwoscia przekroczenia wagi
//     * - jest obe
//     *
//     * Expected:
//     * - user po wyborze typu przejazdu, zostaje przekierowany na informacje o przyczepie
//     * - user po akceptacji informacji o przyczepie, zostaje przekierowany na wybor urzadzenia monitorujacego
//     * - wybor urzadzenia monitorujacego konczy konfiguracje
//     * - mozliwosc zmiany trybu monitorowania, mozliwosc zmiany przyczepy
//     * - automatycznie: pojazd
//     */
//    @Test
//    fun testCase06() {
//        val status = CoreStatus(
//            applicationId = "01",
//            dateTimestamp = 0,
//            vehicles = arrayOf(vehicle_WithOBE_WithTrailer),
//            configuration = defaultConfiguration,
//            messageIds = arrayOf(),
//            sentActivated = false,
//            crmActivated = true,
//            minimumSystemVersion = "",
//            eventStreamConfiguration = defaultEventStreamConfiguration
//        )
//
//        val coordinator: RideConfigurationCoordinator = RideConfigurationCoordinatorImpl()
//        // reset configuration
//        coordinator.resetCoordinator()
//        // initial configuration
//        coordinator.onConfigurationStarted(
//            sentIsPossible = false,
//            tolledIsPossible = true,
//            sentWasOffline = false,
//            sentList = null,
//            status = status
//        )
//        // ride selection screen
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.RIDE_MODE_SELECTION)
//        // select tolled
//        coordinator.onRideModeSelected(tolled = true, sent = false)
//        // check next step
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.TRAILER,
//            coordinator.getNextStep()
//        )
//        // show next step
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.TRAILER)
//        // declare ride with trailer exceeding weight
//        coordinator.onTrailerSelected(noTrailer = false, increaseFlagDeclared = true)
//        // next step should be monitoring device
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE,
//            coordinator.getNextStep()
//        )
//        // show view
//        coordinator.onViewShowing(RideConfigurationCoordinator.RideConfigurationDestination.MONITORING_DEVICE)
//        // select values
//        coordinator.onMonitoringDeviceSelected(true)
//        // we should finish configuration now
//        assertEquals(
//            RideConfigurationCoordinator.RideConfigurationDestination.SAVE_AND_FINISH,
//            coordinator.getNextStep()
//        )
//        // TODO: check output configuration
//    }
//
//}
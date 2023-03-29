package pl.gov.mf.etoll.core.ridecoordinatorv2.filter

//import pl.gov.mf.etoll.core.ridecoordinatorv2.filter.collection.*
//import pl.gov.mf.etoll.networking.api.model.EventStreamLocation
//import junit.framework.TestCase.assertTrue
//import org.junit.Before
//import org.junit.Test
//
//class RideDataFilterDecoratorTest {
//
//    private lateinit var rideDataFilterDecorator: RideDataFilterDecorator
//
//    @Before
//    fun setup() {
//        rideDataFilterDecorator = RideDataFilterDecorator(
//            GpsRuleBE06(), GpsRuleBN06(), GpsRuleBS06(), GpsRuleBW06(),
//            GpsRuleLESEUA(), GpsRuleLSSWCZ(), GpsRuleSNERU()
//        )
//    }
//
//    @Test
//    fun isDataValid() {
//        val rybnik = EventStreamLocation(
//            "1599040507",
//            "6f484d6249467242717058616e49525849694d4a764875587152564364635700_M20-NZ06YC-2",
//            50.09182166666666,
//            18.553836666666665, 0.0, 1599040507, 0.0, 20.0, 0.0,
//            "location", "0", "0", "0", "0"
//        )
//        assertTrue(rideDataFilterDecorator.isDataValid(rybnik))
//
//        val warszawa = EventStreamLocation(
//            "1599041181",
//            "6f484d6249467242717058616e49525849694d4a764875587152564364635700_M20-NZ06YC-2",
//            52.229675, 21.012228333333336, 0.0, 1599041181,
//            0.0, 20.0, 0.0,
//            "location", "0", "0", "0", "0"
//        )
//        assertTrue(rideDataFilterDecorator.isDataValid(warszawa))
//
//        val rzeszow = EventStreamLocation(
//            "1599045724",
//            "6f484d6249467242717058616e49525849694d4a764875587152564364635700_M20-NZ06YC-2",
//            50.04118666666667, 21.99911833333333, 0.0,
//            1599045724, 0.0, 20.0, 0.0, "location",
//            "0", "0", "0", "0"
//        )
//        assertTrue(rideDataFilterDecorator.isDataValid(rzeszow))
//
//        val jastrzebiaGora = EventStreamLocation(
//            "1599062541",
//            "6f484d6249467242717058616e49525849694d4a764875587152564364635700_M20-YH25YM-2",
//            54.83117000000001,
//            18.312878333333334,
//            0.0,
//            1599062541,
//            0.0,
//            20.0,
//            0.0,
//            "location",
//            "0",
//            "0",
//            "0",
//            "0"
//        )
//        assertTrue(rideDataFilterDecorator.isDataValid(jastrzebiaGora))
//
//        val wolosate = EventStreamLocation(
//            "1599063225",
//            "6f484d6249467242717058616e49525849694d4a764875587152564364635700_M20-YH25YM-2",
//            49.06731166666667,
//            22.679731666666665,
//            0.0,
//            1599063225,
//            0.0,
//            20.0,
//            0.0,
//            "location",
//            "0",
//            "0",
//            "0",
//            "0"
//        )
//        assertTrue(rideDataFilterDecorator.isDataValid(wolosate))
//
//        val zosin = EventStreamLocation(
//            "1599040507",
//            "6f484d6249467242717058616e49525849694d4a764875587152564364635700_M20-NZ06YC-2",
//            50.09182166666666,
//            18.553836666666665,
//            0.0,
//            1599040507,
//            0.0,
//            20.0,
//            0.0,
//            "location",
//            "0",
//            "0",
//            "0",
//            "0"
//        )
//        assertTrue(rideDataFilterDecorator.isDataValid(zosin))
//
//        val staryKostrzynek = EventStreamLocation(
//            "1599064264",
//            "6f484d6249467242717058616e49525849694d4a764875587152564364635700_M20-YH25YM-2",
//            52.83164333333334,
//            14.14153,
//            0.0,
//            1599064264,
//            0.0,
//            20.0,
//            0.0,
//            "location",
//            "0",
//            "0",
//            "0",
//            "0"
//        )
//        assertTrue(rideDataFilterDecorator.isDataValid(staryKostrzynek))
//    }
//}
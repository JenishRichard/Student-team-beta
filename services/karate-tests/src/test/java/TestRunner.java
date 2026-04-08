import com.intuit.karate.junit5.Karate;

class TestRunner {

    @Karate.Test
    Karate runRoomTests() {
        return Karate.run("classpath:features/roomService.feature").relativeTo(getClass());
    }

    @Karate.Test
    Karate runBookingTests() {
        return Karate.run("classpath:features/bookingService.feature").relativeTo(getClass());
    }
}

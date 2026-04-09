import com.intuit.karate.junit5.Karate;

class TestRunner {

    @Karate.Test
    Karate runRoomTests() {
        return Karate.run("features/roomService", "features/bookingService").relativeTo(getClass());
    }
}

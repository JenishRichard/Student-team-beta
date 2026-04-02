import com.intuit.karate.junit5.Karate;

class TestRunner {

    @Karate.Test
    Karate runAll() {
        return Karate.run("classpath:features");
    }
}

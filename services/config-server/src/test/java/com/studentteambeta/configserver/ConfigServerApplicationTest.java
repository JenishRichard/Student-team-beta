package com.studentteambeta.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.main.web-application-type=none"
    }
)
@ActiveProfiles("native")
class ConfigServerApplicationTest {

    @Test
    void contextLoads() {
    }
}

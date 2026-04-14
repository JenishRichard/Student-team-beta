package com.classroom.booking_service.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class DisableConfigImportEnvironmentPostProcessorTest {

    private final DisableConfigImportEnvironmentPostProcessor processor = new DisableConfigImportEnvironmentPostProcessor();

    @AfterEach
    void cleanup() {
        System.clearProperty("disable.config.import");
        // clear any env var won't be possible here, but tests avoid setting it
    }

    @Test
    void addsPropertyWhenTestProfileActive() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("test");

        processor.postProcessEnvironment(env, null);

        assertEquals("", env.getProperty("spring.config.import"));
    }

    @Test
    void addsPropertyWhenSystemPropertySet() {
        MockEnvironment env = new MockEnvironment();

        System.setProperty("disable.config.import", "true");

        processor.postProcessEnvironment(env, null);

        assertEquals("", env.getProperty("spring.config.import"));
    }
}

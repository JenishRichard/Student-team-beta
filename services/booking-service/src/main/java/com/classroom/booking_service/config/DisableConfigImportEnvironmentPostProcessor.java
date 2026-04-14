package com.classroom.booking_service.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

import java.util.HashMap;
import java.util.Map;

/**
 * EnvironmentPostProcessor that disables spring.config.import when running
 * with the 'test' profile (or when system property disable.config.import=true).
 * This prevents the application from trying to contact an external config-server
 * during unit tests.
 */
public class DisableConfigImportEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_NAME = "spring.config.import";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // If tests are running with 'test' profile, or user explicitly disables import,
        // set spring.config.import to empty to avoid external config-server resolution.
        boolean isTestProfile = environment.acceptsProfiles(Profiles.of("test"));
        boolean explicitDisable = "true".equalsIgnoreCase(System.getProperty("disable.config.import"))
                || "true".equalsIgnoreCase(System.getenv("DISABLE_CONFIG_IMPORT"));

        if (isTestProfile || explicitDisable) {
            // Only add if not already present (don't override explicit non-empty settings)
            String current = environment.getProperty(PROPERTY_NAME);
            if (current == null || current.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put(PROPERTY_NAME, "");
                environment.getPropertySources().addFirst(new MapPropertySource("disableConfigImport", map));
            }
        }
    }

    @Override
    public int getOrder() {
        // run early
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

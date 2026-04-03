package com.classroom.booking_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.classroom.booking_service.config.RestTemplateConfig;

import static org.junit.jupiter.api.Assertions.*;

class RestTemplateConfigTest {

    @Test
    void testRestTemplateBeanCreation() {

        RestTemplateConfig config = new RestTemplateConfig();

        RestTemplate restTemplate = config.restTemplate();

        assertNotNull(restTemplate);
    }
}
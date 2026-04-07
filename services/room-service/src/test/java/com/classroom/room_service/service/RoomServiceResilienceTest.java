package com.classroom.room_service.service;

import com.classroom.room_service.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = RoomServiceResilienceTest.TestConfig.class,
        properties = {
                "custom.delay-ms=250",
                "resilience4j.timelimiter.instances.roomServiceTimeout.timeout-duration=50ms",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                        + "org.flywaydb.autoconfigure.FlywayAutoConfiguration"
        }
)
class RoomServiceResilienceTest {

    @Autowired
    private RoomService roomService;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void getRoomDetails_shouldReturnFallbackWhenTimeoutExceeded() {
        com.classroom.room_service.entity.Room room = new com.classroom.room_service.entity.Room();
        room.setId(1L);
        room.setRoomNumber("A101");
        room.setBuilding("Main Block");
        room.setCapacity(40);
        room.setType("LECTURE");
        room.setAvailable(true);
        org.mockito.Mockito.when(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(room));

        var result = roomService.getRoomDetails(1L, "Bearer token").join();

        assertThat(result.bookingStatus()).isEqualTo("UNKNOWN");
        assertThat(result.bookingMessage()).isEqualTo("Booking service unavailable");
        assertThat(result.roomNumber()).isEqualTo("A101");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(RoomServiceImpl.class)
    static class TestConfig {
    }
}

package com.classroom.booking_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void gettersAndSettersWork() {
        ErrorResponse r = new ErrorResponse();
        r.setMessage("m");
        r.setStatus(500);

        assertEquals("m", r.getMessage());
        assertEquals(500, r.getStatus());

        ErrorResponse r2 = new ErrorResponse("err", 400);
        assertEquals("err", r2.getMessage());
        assertEquals(400, r2.getStatus());
    }
}

package io.keepup.cms.core.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void build() {
        var testSuccess = ValidationResult.build(true, "testSuccess");
        assertTrue(testSuccess.isSuccess());
    }

    @Test
    void error() {
        var testError = ValidationResult.error("testError");
        assertFalse(testError.isSuccess());
        assertEquals("testError", testError.getMessage());
    }

}
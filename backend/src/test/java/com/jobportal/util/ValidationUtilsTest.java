package com.jobportal.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

    /*@ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user.name+tag@domain.co.in", "admin@portal.com"})
    @DisplayName("Should return true for valid email formats")
    public void testValidEmails(String email) {
        assertTrue(ValidationUtils.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "@missinguser.com", "user@.missingdomain", "user@domain..com"})
    @DisplayName("Should return false for invalid email formats")
    public void testInvalidEmails(String email) {
        assertFalse(ValidationUtils.isValidEmail(email));
    }*/

    @ParameterizedTest
    @CsvSource({
            "test@example.com, true",
            "user.name+tag@domain.co.in, true",
            "admin@portal.com, true",
            "plainaddress, false",
            "@missinguser.com, false",
            "user@.missingdomain, false",
            "user@domain..com, false"
    })
    @DisplayName("Should validate email formats correctly")
    public void testEmailValidation(String email, boolean expectedResult) {
        assertEquals(expectedResult, ValidationUtils.isValidEmail(email));
    }

    @Test
    @DisplayName("Should throw an exception for invalid password format")
    public void testValidatePasswordThrowsException() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ValidationUtils.isValidPassword("123"));

        assertEquals("Password must be at least 6 characters long", exception.getMessage());
    }

    @Test
    @DisplayName("Should not throw an exception for valid password format")
    public void testValidatePasswordSuccess() {
        assertDoesNotThrow(() -> ValidationUtils.isValidPassword("123456"));
    }

    @RepeatedTest(value = 5, name = "Repeating password validation: {currentRepetition} of {totalRepetition}")
    @DisplayName("Should repeatedly validate passsword requirements")
    public void testValidatePasswordSuccessRepeated(RepetitionInfo repetitionInfo) {
        System.out.println("Running iteratiom: " + repetitionInfo.getCurrentRepetition());

        assertDoesNotThrow(() -> ValidationUtils.isValidPassword("validPsw123"));
    }
}

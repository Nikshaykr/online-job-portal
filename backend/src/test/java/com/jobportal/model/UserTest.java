package com.jobportal.model;

import org.junit.jupiter.api.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Nikshay", "nik@test.com", "secure123", Role.SEEKER);
    }

    @Test
    @DisplayName("Should successfully read initial values set by constructor")
    void testInitialValues() {
        assertAll("Verify constructor initialization",
                () -> assertEquals("Nikshay", testUser.getName()),
                () -> assertEquals("nik@test.com", testUser.getEmail()),
                () -> assertEquals("secure123", testUser.getPassword()),
                () -> assertEquals(Role.SEEKER, testUser.getRole()),
                () -> assertNull(testUser.getResumePath())
        );
    }

    @Test
    @DisplayName("Should update username when setName is called")
        void testSetName() {
            testUser.setName("New Name");

            assertEquals("New Name", testUser.getName());
        }
    @Test
    @DisplayName("Should update resume path when setResumePath is called")
    void testSetResumePath() {
        testUser.setResumePath("uploads/resume_1.pdf");

        assertNotNull(testUser.getResumePath());
        assertEquals("uploads/resume_1.pdf", testUser.getResumePath());
    }

    @Test
    @DisplayName("Should return correct authority mapped from Role Enum for UserDetails")
    void testGetAuthorities() {
        assertTrue(testUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SEEKER")));
    }
}
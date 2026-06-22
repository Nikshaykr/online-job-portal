package com.jobportal.model;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {

    @BeforeAll
    public static void initTestSuite() {
        System.out.println("--- Starting Application Entity Test Suite ---");
    }

    @AfterAll
    public static void cleanTestSuite() {
        System.out.println("--- Finished Application Entity Test Suite ---");
    }

    @Nested
    @DisplayName("Test for Application Initialization")
    class InitializationTest {
        private Application application;

        @BeforeEach
        void setUp() {
            application = new Application();
        }

        @Test
        @DisplayName("Default constructor should initialize fields to null")
        public void testDefaultConstructor() {
            assertAll("Verify default null state",
                    () -> assertNull(application.getId()),
                    () -> assertNull(application.getJob()),
                    () -> assertNull(application.getSeeker()),
                    () -> assertNull(application.getStatus()),
                    () -> assertNull(application.getAppliedDate())
            );
        }
    }

    @Nested
    @DisplayName("Test for Application Getters, Setters & Object Graphs")
    class GetterSetterTest {
        private Application application;
        private LocalDate testDate;

        @BeforeEach
        void setUp() {
            application = new Application();
            testDate = LocalDate.now();

            // Set up a mock job relationship context manually
            Job mockJob = new Job();
            mockJob.setTitle("Backend Engineer");
            mockJob.setCompany("Google");

            // Set up a mock seeker relationship context manually
            User mockSeeker = new User("Nikshay", "nik@test.com", "secure123", Role.SEEKER);

            // Populate application object graph
            application.setId(1L);
            application.setJob(mockJob);
            application.setSeeker(mockSeeker);
            application.setStatus("PENDING");
            application.setAppliedDate(testDate);
        }

        @Test
        @DisplayName("Should successfully change and retrieve the application status")
        public void testSetStatus() {
            application.setStatus("SHORTLISTED");
            assertEquals("SHORTLISTED", application.getStatus());
        }

        @Test
        @DisplayName("Should navigate the database relationship graph successfully without null entries")
        public void testRelationshipGraphNavigation() {
            assertAll("Verify multi-entity object graph mapping matches exactly",
                    () -> assertEquals(1L, application.getId()),
                    () -> assertNotNull(application.getJob()),
                    () -> assertEquals("Backend Engineer", application.getJob().getTitle()),
                    () -> assertEquals("Google", application.getJob().getCompany()),
                    () -> assertNotNull(application.getSeeker()),
                    () -> assertEquals("Nikshay", application.getSeeker().getName()),
                    () -> assertEquals(testDate, application.getAppliedDate())
            );
        }
    }
}
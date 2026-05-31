package com.jobportal.model;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JobTest {

    @BeforeAll
    public static void initTestSuite() {
        System.out.println("--- Starting Job Entity Test Suite ---");
    }

    @AfterAll
    public static void cleanTestSuite() {
        System.out.println("--- Finished Job Entity Test Suite ---");
    }

    @Nested
    @DisplayName("Test for Job Initialization")
    class InitializationTest {
        private Job job;

        @BeforeEach
        void setUp() {
            job = new Job();
        }

        @Test
        @DisplayName("Default constructor should initialize job to null")
        public void testDefaultConstructor() {
            assertAll("Verify default null state",
                    () -> assertNull(job.getTitle()),
                    () -> assertNull(job.getEmployerId()),
                    () -> assertNull(job.getPostedDate())
            );
        }
    }

    @Nested
    @DisplayName("Test for Job Getters and Setters")
    class GetterSetterTest {
        private Job job;

        @BeforeEach
        void setUp() {
            job = new Job();

            job.setTitle("Software Engineer");
            job.setEmployerId(10L);
            job.setPostedDate(LocalDate.now());
        }

        @Test
        @DisplayName("Should successfully change and receive the title")
        public void testSetTitle() {
            job.setTitle("Senior Dev");
            assertEquals("Senior Dev", job.getTitle());
        }

        @Test
        @DisplayName("Should successfully retrieve Employer ID and Posted Date")
        public void testJobMeta() {
            assertAll("Verify getters",
                    () -> assertEquals(10L, job.getEmployerId()),
                    () -> assertEquals(LocalDate.now(), job.getPostedDate())
            );
        }
    }
}

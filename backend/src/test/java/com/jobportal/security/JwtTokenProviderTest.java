package com.jobportal.security;

import com.jobportal.model.Role;
import com.jobportal.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String TEST_SECRET = "my-test-secret-key-which-is-long-enough-1234567890";
    private static final long ONE_HOUR_MS = 3_600_000L;

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Fresh provider + user before every test, so no test affects another.
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, ONE_HOUR_MS);

        testUser = new User("Nikshay", "nik@test.com", "secure123", Role.EMPLOYER);
        testUser.setId(42L);
    }

    @Test
    @DisplayName("Generated token should be a non-empty, three-part JWT (header.payload.signature)")
    void generateToken_producesValidStructure() {
        String token = jwtTokenProvider.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length, "A JWT has three dot-separated parts");
    }

    @Test
    @DisplayName("A freshly generated token should pass validation")
    void validateToken_returnsTrueForValidToken() {
        String token = jwtTokenProvider.generateToken(testUser);

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Claims read back from the token should match the original user")
    void extractedClaims_shouldMatchUser() {
        String token = jwtTokenProvider.generateToken(testUser);

        // assertAll runs every check even if an earlier one fails, so we see
        // ALL mismatches at once instead of just the first.
        assertAll("token claims",
                () -> assertEquals(42L, jwtTokenProvider.getUserIdFromToken(token)),
                () -> assertEquals("Nikshay", jwtTokenProvider.getUserNameFromToken(token)),
                () -> assertEquals(Role.EMPLOYER, jwtTokenProvider.getUserRoleFromToken(token))
        );
    }

    @Test
    @DisplayName("An expired token should fail validation")
    void validateToken_returnsFalseForExpiredToken() {
        // A negative expiry means expiryDate = now - 1s, so the token is born already expired.
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, -1_000L);
        String expiredToken = expiredProvider.generateToken(testUser);

        assertFalse(expiredProvider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("A token signed with a different secret should fail validation (no forgery)")
    void validateToken_returnsFalseForWrongSignature() {
        String token = jwtTokenProvider.generateToken(testUser);

        // A provider with a DIFFERENT secret cannot verify our signature.
        JwtTokenProvider otherProvider =
                new JwtTokenProvider("a-totally-different-secret-key-also-long-enough-99", ONE_HOUR_MS);

        assertFalse(otherProvider.validateToken(token));
    }

    @Test
    @DisplayName("A tampered token should fail validation")
    void validateToken_returnsFalseForTamperedToken() {
        String token = jwtTokenProvider.generateToken(testUser);

        // Flip the final character of the signature; the signature no longer matches.
        char last = token.charAt(token.length() - 1);
        String tamperedToken = token.substring(0, token.length() - 1) + (last == 'a' ? 'b' : 'a');

        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("A malformed (non-JWT) string should fail validation, not throw")
    void validateToken_returnsFalseForGarbage() {
        assertFalse(jwtTokenProvider.validateToken("this.is.not.a.jwt"));
    }
}

package com.lms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    private UserDetails userDetails;
    private String secret = "mySuperSecretKeyForJwtTokenGenerationAndValidationThatIsAtLeast32BytesLong";
    private Long expiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", expiration);

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtTokenUtil.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        String username = jwtTokenUtil.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtTokenUtil.generateToken(userDetails);

        String username = jwtTokenUtil.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        String token = jwtTokenUtil.generateToken(userDetails);
        Date now = new Date();

        Date expiration = jwtTokenUtil.extractExpiration(token);

        assertThat(expiration).isAfter(now);
    }

    @Test
    void validateToken_ShouldReturnTrue_ForValidToken() {
        String token = jwtTokenUtil.generateToken(userDetails);

        boolean isValid = jwtTokenUtil.validateToken(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForInvalidUsername() {
        String token = jwtTokenUtil.generateToken(userDetails);

        UserDetails wrongUser = User.builder()
                .username("wronguser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        boolean isValid = jwtTokenUtil.validateToken(token, wrongUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForMalformedToken() {
        boolean isValid = jwtTokenUtil.validateToken("malformed", userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void extractClaim_ShouldReturnCorrectClaim() {
        String token = jwtTokenUtil.generateToken(userDetails);

        String subject = jwtTokenUtil.extractClaim(token, Claims::getSubject);

        assertThat(subject).isEqualTo("testuser");
    }

    @Test
    void generateToken_WithDifferentUsers_ShouldCreateDifferentTokens() {
        String token1 = jwtTokenUtil.generateToken(userDetails);

        UserDetails anotherUser = User.builder()
                .username("anotheruser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        String token2 = jwtTokenUtil.generateToken(anotherUser);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void getSigningKey_ShouldCreateKeyFromSecret() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
    }

    @Test
    void validateToken_ShouldHandleNullToken() {
        boolean isValid = jwtTokenUtil.validateToken(null, userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldHandleNullUserDetails() {
        String token = jwtTokenUtil.generateToken(userDetails);

        boolean isValid = jwtTokenUtil.validateToken(token, null);

        assertThat(isValid).isFalse();
    }

    @Test
    void token_ShouldHaveCorrectFormat() {
        String token = jwtTokenUtil.generateToken(userDetails);

        assertThat(token).matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$");
    }
}
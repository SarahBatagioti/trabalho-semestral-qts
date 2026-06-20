package br.com.AllTallent.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET);
    }

    @Test
    void shouldGenerateTokenAndExtractClaims() {
        UserDetails user = User.withUsername("ana@mail.com").password("secret").authorities("ROLE_USER").build();

        String token = jwtService.generateToken(Map.of("scope", "read"), user);
        String tokenWithoutClaims = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("ana@mail.com");
        String scope = jwtService.extractClaim(token, claims -> claims.get("scope", String.class));
        assertThat(scope).isEqualTo("read");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
        assertThat(jwtService.extractUsername(tokenWithoutClaims)).isEqualTo("ana@mail.com");
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        UserDetails originalUser = User.withUsername("ana@mail.com").password("secret").authorities("ROLE_USER").build();
        UserDetails anotherUser = User.withUsername("bia@mail.com").password("secret").authorities("ROLE_USER").build();

        String token = jwtService.generateToken(originalUser);

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        UserDetails user = User.withUsername("ana@mail.com").password("secret").authorities("ROLE_USER").build();
        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now.minusSeconds(120)))
                .setExpiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)), SignatureAlgorithm.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, user))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldThrowForMalformedToken() {
        assertThatThrownBy(() -> jwtService.extractClaim("invalid-token", Claims::getSubject))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldReturnFalseWhenUsernameMatchesButExpirationIsInPastUsingSpy() {
        JwtService spyService = org.mockito.Mockito.spy(jwtService);
        UserDetails user = User.withUsername("ana@mail.com").password("secret").authorities("ROLE_USER").build();
        java.util.function.Function<Claims, Date> expirationResolver = Claims::getExpiration;
        org.mockito.Mockito.doReturn("ana@mail.com").when(spyService).extractUsername("token");
        org.mockito.Mockito.doReturn(Date.from(Instant.now().minusSeconds(60)))
                .when(spyService)
                .extractClaim(org.mockito.Mockito.eq("token"), org.mockito.ArgumentMatchers.any());

        assertThat(spyService.isTokenValid("token", user)).isFalse();
    }
}

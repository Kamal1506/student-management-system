package com.sms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Pulled from application.properties → jwt.secret
    @Value("${jwt.secret}")
    private String secret;

    // Pulled from application.properties → jwt.expiration (milliseconds)
    // Default: 86400000 = 24 hours
    @Value("${jwt.expiration}")
    private long expiration;

    // ─── GENERATE TOKEN ───────────────────────────────────────────────────────

    /**
     * Creates a JWT token for the logged-in user.
     * The token contains: username (subject), role, issued-at, expiry.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        // Store the user's role inside the token so we know ADMIN / TEACHER / STUDENT
        extraClaims.put("role", userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(Object::toString)
                .orElse(""));

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())      // who this token belongs to
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ─── VALIDATE TOKEN ───────────────────────────────────────────────────────

    /**
     * Returns true if the token belongs to this user AND hasn't expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ─── EXTRACT DATA FROM TOKEN ──────────────────────────────────────────────

    /** Get the username (subject) stored in the token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Get the role stored in the token. */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /** Get the expiry date from the token. */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ─── INTERNAL HELPERS ─────────────────────────────────────────────────────

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Converts your plain-text secret into a cryptographic key.
     * The secret must be Base64-encoded in application.properties.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
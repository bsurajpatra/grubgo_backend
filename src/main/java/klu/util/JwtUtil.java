package klu.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;
    
    private Key signingKey;
    
    @PostConstruct
    public void init() {
        logger.info("Initializing JWT utility");
        if (secret == null || secret.isBlank()) {
            logger.error("JWT secret is missing or empty");
            throw new IllegalStateException("JWT secret must be configured");
        }
        logger.info("JWT secret loaded successfully with length: {}", secret.length());
        
        // Initialize signing key once
        signingKey = getSigningKey();
        logger.info("JWT signing key initialized successfully");
    }

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Failed to extract claims from token: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours token validity
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
            logger.debug("Generated new token for user: {}", subject);
            return token;
        } catch (Exception e) {
            logger.error("Failed to create token: {}", e.getMessage());
            throw e;
        }
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String role = extractRole(token);
            
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            
            if (!isValid) {
                if (!username.equals(userDetails.getUsername())) {
                    logger.warn("Token username '{}' doesn't match UserDetails username '{}'", 
                            username, userDetails.getUsername());
                }
                if (isTokenExpired(token)) {
                    logger.warn("Token is expired");
                }
            }
            
            logger.debug("Token validated for user: {}, role: {}, valid: {}", 
                    username, role, isValid);
            
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }
}

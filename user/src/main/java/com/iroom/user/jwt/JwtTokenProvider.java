package com.iroom.user.jwt;

import com.iroom.user.entity.Admin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey = Keys.hmacShaKeyFor("secret-key-to-get-from-spring-cloud-config".getBytes(StandardCharsets.UTF_8));
    private final long expirationMs = 86400000;

    public String createToken(Admin admin) {
        Claims claims = Jwts.claims().setSubject(admin.getId().toString());
        claims.put("email", admin.getEmail());
        claims.put("role", admin.getRole());

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}

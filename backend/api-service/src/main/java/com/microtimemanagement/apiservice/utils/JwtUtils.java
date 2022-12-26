package com.microtimemanagement.apiservice.utils;

import com.microtimemanagement.apiservice.model.User;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:secret}")
    private String key;

    public String generateToken(User user, Map<String, Object> claims){
        log.info("Using key: {}", key);
        List<String> authorities = user.
                getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        log.info("Authorities generated: {}", authorities);
        return Jwts.builder()
                .addClaims(claims)
                .claim("authorities", authorities)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)))
                .signWith(SignatureAlgorithm.HS512, key).compact();
    }

    public String extractUserNameFromToken(String token){
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
    }

    private Jws<Claims> parseToken(String token){
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token);
    }

    public Boolean isTokenExpired(String token){
        Jws<Claims> tokenClaims = parseToken(token);
        return tokenClaims.getBody()
                .getExpiration().getTime() <= System.currentTimeMillis();
    }
    public Boolean isValidTokenSubject(String token, User user){
        Jws<Claims> tokenClaims = parseToken(token);
        log.info("Parsed claims: {}", tokenClaims);
        return tokenClaims.getBody().getSubject().equals(user.getUsername());
    }
}

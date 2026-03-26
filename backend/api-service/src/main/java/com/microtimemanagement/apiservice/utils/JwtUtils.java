package com.microtimemanagement.apiservice.utils;

import com.microtimemanagement.apiservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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

    private SecretKey getSigningKey(){
        log.info("Using key: {}", key);
//        Decoders.BASE64.decode(key)
        byte [] secretKeyStringBytes = (key + key + key + key + key).getBytes();
        return Keys.hmacShaKeyFor(secretKeyStringBytes);
    }

    public String generateToken(User user, Map<String, Object> claims){
        List<String> authorities = user.
                getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        log.info("Authorities generated: {}", authorities);
//        return Jwts.builder()
//                .addClaims(claims)
//                .claim("authorities", authorities)
//                .setSubject(user.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)))
//                .signWith(SignatureAlgorithm.HS512, key).compact();
        return Jwts.builder()
                .claims(claims)
                .claim("authorities", authorities)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();

    }

    public String extractUserNameFromToken(String token){
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload().getSubject();
//                .setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
    }

    private Jws<Claims> parseToken(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey()).build().parseSignedClaims(token);
//                .setSigningKey(key).parseClaimsJws(token);
    }

    public Boolean isTokenExpired(String token){
        Jws<Claims> tokenClaims = parseToken(token);
        return tokenClaims.getPayload()
                .getExpiration().getTime() <= System.currentTimeMillis();
    }
    public Boolean isValidTokenSubject(String token, User user){
        Jws<Claims> tokenClaims = parseToken(token);
        log.info("Parsed claims: {}", tokenClaims);
        return tokenClaims.getPayload().getSubject().equals(user.getUsername());
    }
}

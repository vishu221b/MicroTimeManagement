package com.microtimemanagement.apiservice.utils;

import com.microtimemanagement.apiservice.dto.request.JwtCreationRequestDTO;
import com.microtimemanagement.apiservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:secret}")
    private String key;

    private SecretKey getSigningKey(){
        log.info("Using key: {}", key);
        byte [] secretKeyStringBytes = (key + key + key + key + key).getBytes();
        return Keys.hmacShaKeyFor(secretKeyStringBytes);
    }
    public String generateToken(JwtCreationRequestDTO requestDTO){
        return Jwts.builder()
                .claims(requestDTO.getAdditionalClaims())
                .claim("authorities", requestDTO.getPrincipalAuthorities())
                .subject(requestDTO.getPrincipal())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(requestDTO.getExpiry())
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();

    }

    public String extractPrincipalFromToken(String token){
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public Jws<Claims> parseToken(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey()).build().parseSignedClaims(token);
    }

    public Boolean isTokenExpired(String token){
        Boolean tokenExpired = Boolean.FALSE;
        try{
            parseToken(token);
        }catch (ExpiredJwtException exception){
            log.info("Expired JWT Token : {}", exception.getMessage());
            tokenExpired = Boolean.TRUE;
        }
        return tokenExpired;
    }
    public Boolean isValidTokenSubject(String token, User user){
        Jws<Claims> tokenClaims = parseToken(token);
        log.info("Parsed claims: {}", tokenClaims);
        return tokenClaims.getPayload().getSubject().equals(user.getUid());
    }
}

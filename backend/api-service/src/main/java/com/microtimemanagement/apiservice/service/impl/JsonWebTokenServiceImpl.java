package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.dto.request.JwtCreationRequestDTO;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.service.JsonWebTokenService;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * This Service is a Wrapper around {@link JwtUtils} class.
 * @see JwtUtils
 *
 * @author vishal.dogra
 * @since 1.0.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonWebTokenServiceImpl implements JsonWebTokenService {

    private final JwtUtils jwtUtils;

    /**
     * @implNote This method creates a new JWT Access Token for the given subject and claims in the request dto.
     * @param requestDTO JWT Token Creation Request DTO
     * @return {@link String}
     * @since 1.0.0
     * @see JwtCreationRequestDTO
     */
    @Override
    public String createNewToken(JwtCreationRequestDTO requestDTO) {
        return jwtUtils.generateToken(requestDTO);
    }

    /**
     * @implNote This method extracts the subject from a valid JWT Access token.
     * @param token JWT Session Access Token
     * @return {@link String}
     * @since 1.0.0
     */
    @Override
    public String getPrincipalSubjectForToken(String token) {
        return jwtUtils.extractPrincipalFromToken(token);
    }

    /**
     * @implNote This method checks if the given Jwt Session Access Token is expired.
     *
     * @param token Jwt session access token
     * @return {@link Boolean}
     *
     * @since 1.0.0
     */
    @Override
    public Boolean isJwtTokenExpired(String token) {
        return jwtUtils.isTokenExpired(token);
    }

    /**
     * @implNote This method checks if the given user entity is the valid token subject for the given JWT token.
     *
     * @param token JWT session access token of the current signed in user
     * @param user User entity who needs to be checked as token subject
     *
     * @return {@link Boolean}
     *
     * @since 1.0.0
     */
    @Override
    public Boolean tokenSubjectIsValid(String token, User user) {
        return jwtUtils.isValidTokenSubject(token, user);
    }
}

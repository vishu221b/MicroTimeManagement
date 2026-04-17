package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.constants.ResponseMessages;
import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.dto.entity.ValidSessionDTO;
import com.microtimemanagement.apiservice.dto.request.AuthenticationRequestDTO;
import com.microtimemanagement.apiservice.dto.response.AuthenticationLoginResponseDTO;
import com.microtimemanagement.apiservice.dto.response.GenericMessageResponseDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.model.User;
import com.microtimemanagement.apiservice.service.AccessTokenService;
import com.microtimemanagement.apiservice.service.AuthenticationAndAuthorizationService;
import com.microtimemanagement.apiservice.service.SessionService;
import com.microtimemanagement.apiservice.service.UserService;
import com.microtimemanagement.apiservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationAndAuthorizationServiceImpl implements AuthenticationAndAuthorizationService {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    private final SessionService sessionService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AccessTokenService accessTokenService;

    @Override
    public AuthenticationLoginResponseDTO microTimeManagementSessionLogin(
            AuthenticationRequestDTO authenticationRequestDTO
    ) {
        User user = userService.loadUserByUsername(authenticationRequestDTO.getUsername());
        if(bCryptPasswordEncoder.matches(authenticationRequestDTO.getPassword(), user.getPassword())){
            SessionDTO sessionDTO = sessionService.createNewSession(user);
            return AuthenticationLoginResponseDTO.builder()
                    .accessToken(sessionDTO.getRefreshTokenDTO().getActiveAccessTokenDTO().getToken())
                    .refreshToken(sessionDTO.getRefreshTokenDTO().getToken())
                    .build();
        }
        throw new MicroTimeManagementBadRequestException(ErrorConstants.INVALID_PASSWORD_VALUE);
    }

    @Override
    public GenericMessageResponseDTO<?> microTimeManagementSessionLogout(String token) {
        sessionService.destroySession(token);
        return GenericMessageResponseDTO.builder().message(ResponseMessages.LOGOUT_SUCCESS).build();
    }

    @Override
    public ValidSessionDTO validateCurrentUserSessionForAccessToken(String token) {
        String error = null;
        Boolean isValidToken = Boolean.FALSE;
        final boolean isExpired = jwtUtils.isTokenExpired(token);
        if(isExpired){
            error = ErrorConstants.SESSION_EXPIRED;
        }

        final String principal = jwtUtils.extractPrincipalFromToken(token);

        User user = null;
        if(null!=principal && !principal.isEmpty()){
            user = userService.getUserByUid(principal);
            isValidToken = jwtUtils.isValidTokenSubject(token, user);
        }
        if(isValidToken && !isExpired){
            /*
             * TODO: Use accessTokenService instead of SessionService to verify token
             * TODO: For Refresh tokens, the API would itself be responsible for verifying that and issuing new accessTokens
             */
            AccessTokenDTO accessTokenDTO = accessTokenService.findAccessToken(token);
            if(null == accessTokenDTO){
                log.error("No session found for token: {}, giving error", token);
                return ValidSessionDTO.builder()
                        .isValidSession(Boolean.FALSE)
                        .principal(null)
                        .error(ErrorConstants.SESSION_TOKEN_INVALID)
                        .build();
            }
        }
        if(!isValidToken){
            error = ErrorConstants.SESSION_TOKEN_INVALID;
        }
        final Boolean isValid = !isExpired && null!=principal && isValidToken;
        log.info("isValidSession for isValid: {} and user: {} with error: {}", isValid, user, error);
        return ValidSessionDTO.builder()
                .isValidSession(isValid)
                .principal(user)
                .error(error)
                .build();
    }
}

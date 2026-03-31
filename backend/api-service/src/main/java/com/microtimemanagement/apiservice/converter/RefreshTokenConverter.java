package com.microtimemanagement.apiservice.converter;

import com.microtimemanagement.apiservice.dto.entity.RefreshTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RefreshTokenConverter implements BaseDTOConverter<RefreshToken, RefreshTokenDTO>{


    private final AccessTokenConverter accessTokenConverter;

    @Override
    public RefreshToken fromDTO(RefreshTokenDTO refreshTokenDTO) {
        return null;
    }

    @Override
    public RefreshTokenDTO toDTO(RefreshToken refreshToken) {
        return RefreshTokenDTO.builder()
                .id(refreshToken.getId())
                .activeAccessTokenDTO(
                        accessTokenConverter.toDTO(
                        refreshToken
                                .getAccessTokens()
                                .stream()
                                .filter(
                                        t -> t.getIsActive().equals(Boolean.TRUE)
                                ).toList().get(0))
                )
                .token(refreshToken.getToken())
                .createdAt(refreshToken.getCreatedAt())
                .lastUpdatedAt(refreshToken.getLastUpdatedAt())
                .isActive(refreshToken.getIsActive())
                .build();
    }
}

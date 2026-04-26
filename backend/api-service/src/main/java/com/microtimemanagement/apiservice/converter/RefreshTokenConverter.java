package com.microtimemanagement.apiservice.converter;

import com.microtimemanagement.apiservice.dto.entity.RefreshTokenDTO;
import com.microtimemanagement.apiservice.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        if(null!=refreshToken)
            return RefreshTokenDTO.builder()
                    .id(refreshToken.getId())
                    .token(refreshToken.getToken())
                    .accessTokenDTOList(refreshToken
                            .getAccessTokens()
                            .stream()
                            .map(accessTokenConverter::toDTO)
                            .toList())
                    .createdAt(refreshToken.getCreatedAt())
                    .lastUpdatedAt(refreshToken.getLastUpdatedAt())
                    .isActive(refreshToken.getIsActive())
                    .build();
        return null;
    }
}

package com.microtimemanagement.apiservice.converter;

import com.microtimemanagement.apiservice.dto.entity.AccessTokenDTO;
import com.microtimemanagement.apiservice.dto.entity.RefreshTokenDTO;
import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessTokenConverter implements BaseDTOConverter<AccessToken, AccessTokenDTO>{


    @Override
    public AccessToken fromDTO(AccessTokenDTO accessTokenDTO) {
        return null;
    }

    @Override
    public AccessTokenDTO toDTO(AccessToken accessToken) {
        if (null!=accessToken)
            return AccessTokenDTO.builder()
                    .id(accessToken.getId())
                    .token(accessToken.getToken())
                    .createdAt(accessToken.getCreatedAt())
                    .lastUpdatedAt(accessToken.getLastUpdatedAt())
                    .isActive(accessToken.getIsActive())
                    .expiresAt(accessToken.getExpiresAt())
                    .build();
        return null;
    }
}

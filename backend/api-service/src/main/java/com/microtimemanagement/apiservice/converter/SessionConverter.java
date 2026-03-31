package com.microtimemanagement.apiservice.converter;

import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionConverter implements BaseDTOConverter<Session, SessionDTO>{

    private final RefreshTokenConverter refreshTokenConverter;

    @Override
    public Session fromDTO(SessionDTO sessionDTO) {
        return null;
    }

    @Override
    public SessionDTO toDTO(Session session) {
        return SessionDTO.builder()
                .id(session.getId())
                .refreshTokenDTO(refreshTokenConverter.toDTO(session.getRefreshToken()))
                .createdAt(session.getCreatedAt())
                .lastUpdatedAt(session.getLastUpdatedAt())
                .isActive(session.getIsActive())
                .build();
    }
}

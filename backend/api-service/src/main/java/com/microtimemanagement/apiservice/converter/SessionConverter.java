package com.microtimemanagement.apiservice.converter;

import com.microtimemanagement.apiservice.dto.entity.SessionDTO;
import com.microtimemanagement.apiservice.model.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionConverter implements BaseDTOConverter<Session, SessionDTO>{
    @Override
    public Session fromDTO(SessionDTO sessionDTO) {
        return null;
    }

    @Override
    public SessionDTO toDTO(Session session) {
        return SessionDTO.builder()
                .id(session.getId())
                .token(session.getToken())
                .createdAt(session.getCreatedAt())
                .lastUpdatedAt(session.getLastUpdatedAt())
                .isActive(session.getIsActive())
                .build();
    }
}

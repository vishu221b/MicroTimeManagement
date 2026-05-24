package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.RefreshToken;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RefreshTokenCallbacks implements BeforeConvertCallback<RefreshToken> {
    @Override
    public RefreshToken onBeforeConvert(RefreshToken entity, String collection) {
        Date now = new Date();
        if(null == entity.getCreatedAt())
            entity.setCreatedAt(now);
        entity.setLastUpdatedAt(now);
        if(null == entity.getIsActive())
            entity.setIsActive(true);
        return entity;
    }
}

package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.RefreshToken;
import com.microtimemanagement.apiservice.model.Session;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RefreshTokenCallbacks implements BeforeConvertCallback<RefreshToken> {
    @Override
    public RefreshToken onBeforeConvert(RefreshToken entity, String collection) {
        entity.setCreatedAt(new Date());
        entity.setLastUpdatedAt(new Date());
        entity.setIsActive(true);
        return entity;
    }
}

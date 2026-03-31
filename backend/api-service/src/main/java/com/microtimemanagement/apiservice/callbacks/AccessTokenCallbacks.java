package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.AccessToken;
import com.microtimemanagement.apiservice.model.RefreshToken;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AccessTokenCallbacks implements BeforeConvertCallback<AccessToken> {
    @Override
    public AccessToken onBeforeConvert(AccessToken entity, String collection) {
        entity.setCreatedAt(new Date());
        entity.setLastUpdatedAt(new Date());
        entity.setIsActive(true);
        return entity;
    }
}

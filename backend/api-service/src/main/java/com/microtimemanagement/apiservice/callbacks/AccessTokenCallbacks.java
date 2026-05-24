package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.AccessToken;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AccessTokenCallbacks implements BeforeConvertCallback<AccessToken> {
    @Override
    public AccessToken onBeforeConvert(AccessToken entity, String collection) {
        Date now = new Date();
        if(null == entity.getCreatedAt())
            entity.setCreatedAt(now);
        entity.setLastUpdatedAt(now);
        if(null == entity.getIsActive())
            entity.setIsActive(true);
        return entity;
    }
}

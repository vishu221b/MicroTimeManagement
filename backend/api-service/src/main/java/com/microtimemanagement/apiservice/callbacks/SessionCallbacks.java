package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.Session;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SessionCallbacks implements BeforeConvertCallback<Session> {
    @Override
    public Session onBeforeConvert(Session entity, String collection) {
        Date now = new Date();
        if(null == entity.getCreatedAt())
            entity.setCreatedAt(now);
        entity.setLastUpdatedAt(now);
        if(null == entity.getIsActive())
            entity.setIsActive(true);
        return entity;
    }
}

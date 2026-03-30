package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.User;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class UserCallbacks implements BeforeConvertCallback<User>, AfterConvertCallback<User> {


    @Override
    public User onBeforeConvert(User entity, String collection) {
        // Call at the time of new user creation
        if(null==entity.getCreatedAt()){
            entity.setCreatedAt(new Date());
            entity.setIsActive(true);
            entity.setUid(UUID.randomUUID().toString());
        }
        // Update call edits
        entity.setLastUpdatedAt(new Date());
        return entity;
    }

    @Override
    public User onAfterConvert(User entity, Document document, String collection) {
        return entity;
    }
}
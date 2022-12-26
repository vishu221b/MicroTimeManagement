package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.Role;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RoleCallbacks implements BeforeConvertCallback<Role> {
    @Override
    public Role onBeforeConvert(Role entity, String collection) {
        entity.setCreatedAt(new Date());
        entity.setLastUpdatedAt(new Date());
        entity.setIsActive(true);
        return entity;
    }
}

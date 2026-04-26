package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.ActivityRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class TimeRecordCallbacks implements BeforeConvertCallback<ActivityRecord>{

    @Override
    public ActivityRecord onBeforeConvert(ActivityRecord entity, String collection) {
        entity.setCreatedAt(new Date());
        if(null == entity.getLastUpdatedAt())
            entity.setLastUpdatedAt(new Date());
        if(null == entity.getIsActive())
            entity.setIsActive(true);
        return entity;
    }
}

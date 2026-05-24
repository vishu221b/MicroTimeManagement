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
        Date now = new Date();
        if(null == entity.getCreatedAt())
            entity.setCreatedAt(now);
        entity.setLastUpdatedAt(now);
        if(null == entity.getIsActive())
            entity.setIsActive(true);
        return entity;
    }
}

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
        log.info("Time Record Before Convert Callback -> Entity:{}, Collection:{}", entity, collection);
        entity.setCreatedAt(new Date());
        entity.setLastUpdatedAt(new Date());
        log.info("Time Record After Convert Callback -> Entity:{}, Collection:{}", entity, collection);
        return entity;
    }
}

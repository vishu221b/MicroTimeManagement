package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.ActivityRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityRecordRepository extends MongoRepository<ActivityRecord, String> {

    Optional<ActivityRecord> findByRecordDate(String recordDate);

    Optional<ActivityRecord> findByRecordDateAndCreatedBy(String date, String userId);
}

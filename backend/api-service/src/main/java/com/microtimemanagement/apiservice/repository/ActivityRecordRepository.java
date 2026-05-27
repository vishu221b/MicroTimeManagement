package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.ActivityRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRecordRepository extends MongoRepository<ActivityRecord, String> {

    Optional<ActivityRecord> findByRecordDate(String recordDate);

    Optional<ActivityRecord> findByRecordDateAndCreatedBy(String date, String userId);

    /**
     * Range query over the user's activity records. recordDate is stored as a
     * yyyy-MM-dd string — ISO-8601 ordering matches lexicographic ordering, so
     * Mongo's $gte/$lte work correctly until we migrate the field to LocalDate.
     */
    List<ActivityRecord> findByCreatedByAndRecordDateBetween(
            String createdBy, String fromDate, String toDate);
}

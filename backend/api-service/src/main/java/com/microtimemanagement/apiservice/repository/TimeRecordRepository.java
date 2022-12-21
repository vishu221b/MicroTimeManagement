package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.TimeRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeRecordRepository extends MongoRepository<TimeRecord, String> {

    Optional<TimeRecord> findByRecordForDate(String recordDate);
}

package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.model.ActivityRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, String> {

    Optional<ActivityRecord> findByRecordDate(String recordDate);

    Optional<ActivityRecord> findByRecordDateAndCreatedBy(String date, String userId);

    /**
     * Range query over the user's activity records. recordDate is stored as a
     * yyyy-MM-dd string — ISO-8601 ordering matches lexicographic ordering, so
     * Mongo's $gte/$lte work correctly until we migrate the field to LocalDate.
     */
    List<ActivityRecord> findByCreatedByAndRecordDateBetween(
            String createdBy, String fromDate, String toDate);

    /**
     * Paginated lookup of every record a user owns, ordered by Spring's
     * Pageable sort. The history endpoint pins this to recordDate DESC so the
     * latest day surfaces first.
     */
    Page<ActivityRecord> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * Unpaginated lookup of every record a user owns, ordered by the supplied
     * Sort. Used by the activity-name autocomplete, which needs to walk every
     * record to collect distinct names; recordDate DESC ensures the
     * most-recently-used variant of a name wins the dedup.
     */
    List<ActivityRecord> findByCreatedBy(String createdBy, Sort sort);
}

package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.enums.LinkableType;
import com.microtimemanagement.apiservice.model.EntityLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntityLinkRepository extends JpaRepository<EntityLink, String> {

    List<EntityLink> findByOwnerAndIsActiveTrueOrderByCreatedAtDesc(String owner);

    List<EntityLink> findBySourceTypeAndSourceIdAndOwnerAndIsActiveTrue(
            LinkableType sourceType, String sourceId, String owner);

    Optional<EntityLink> findByIdAndOwnerAndIsActiveTrue(String id, String owner);
}

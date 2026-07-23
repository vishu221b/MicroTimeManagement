package com.microtimemanagement.apiservice.repository;

import com.microtimemanagement.apiservice.enums.AttachmentOwnerType;
import com.microtimemanagement.apiservice.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {

    List<Attachment> findByParentTypeAndParentIdAndUidAndIsActiveTrueOrderByCreatedAtAsc(
            AttachmentOwnerType parentType, String parentId, String uid);

    Optional<Attachment> findByIdAndUidAndIsActiveTrue(String id, String uid);
}

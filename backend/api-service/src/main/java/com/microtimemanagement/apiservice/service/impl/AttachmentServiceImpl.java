package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.AttachmentDTO;
import com.microtimemanagement.apiservice.enums.AttachmentOwnerType;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementBadRequestException;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.Attachment;
import com.microtimemanagement.apiservice.repository.AttachmentRepository;
import com.microtimemanagement.apiservice.service.AttachmentService;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final AttachmentRepository attachmentRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public AttachmentDTO create(AttachmentDTO dto) {
        if (dto.getSizeBytes() != null && dto.getSizeBytes() > MAX_BYTES) {
            throw new MicroTimeManagementBadRequestException(ErrorConstants.FILE_TOO_LARGE);
        }
        Attachment attachment = Attachment.builder()
                .parentType(dto.getParentType())
                .parentId(dto.getParentId())
                .name(dto.getName())
                .contentType(dto.getContentType())
                .sizeBytes(dto.getSizeBytes())
                .dataBase64(dto.getDataBase64())
                .uid(currentUserProvider.currentUid())
                .build();
        return toDTO(attachmentRepository.save(attachment));
    }

    @Override
    public List<AttachmentDTO> listForParent(AttachmentOwnerType parentType, String parentId) {
        return attachmentRepository
                .findByParentTypeAndParentIdAndUidAndIsActiveTrueOrderByCreatedAtAsc(
                        parentType, parentId, currentUserProvider.currentUid())
                .stream().map(this::toDTO).toList();
    }

    @Override
    public void softDelete(String id) {
        Attachment a = attachmentRepository
                .findByIdAndUidAndIsActiveTrue(id, currentUserProvider.currentUid())
                .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.ATTACHMENT_NOT_FOUND));
        a.setIsActive(Boolean.FALSE);
        attachmentRepository.save(a);
    }

    private AttachmentDTO toDTO(Attachment a) {
        return AttachmentDTO.builder()
                .id(a.getId())
                .parentType(a.getParentType())
                .parentId(a.getParentId())
                .name(a.getName())
                .contentType(a.getContentType())
                .sizeBytes(a.getSizeBytes())
                .dataBase64(a.getDataBase64())
                .createdAt(a.getCreatedAt())
                .build();
    }
}

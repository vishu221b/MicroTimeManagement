package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.AttachmentDTO;
import com.microtimemanagement.apiservice.enums.AttachmentOwnerType;

import java.util.List;

public interface AttachmentService {

    AttachmentDTO create(AttachmentDTO dto);

    List<AttachmentDTO> listForParent(AttachmentOwnerType parentType, String parentId);

    void softDelete(String id);
}

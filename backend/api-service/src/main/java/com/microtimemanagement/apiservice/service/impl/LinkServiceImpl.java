package com.microtimemanagement.apiservice.service.impl;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.entity.LinkDTO;
import com.microtimemanagement.apiservice.enums.LinkableType;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementNotFoundException;
import com.microtimemanagement.apiservice.model.EntityLink;
import com.microtimemanagement.apiservice.repository.EntityLinkRepository;
import com.microtimemanagement.apiservice.service.LinkService;
import com.microtimemanagement.apiservice.utils.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final EntityLinkRepository linkRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public LinkDTO create(LinkDTO dto) {
        EntityLink link = EntityLink.builder()
                .sourceType(dto.getSourceType())
                .sourceId(dto.getSourceId())
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .linkType(dto.getLinkType())
                .owner(currentUserProvider.currentUid())
                .build();
        return toDTO(linkRepository.save(link));
    }

    @Override
    public List<LinkDTO> listForCurrentUser() {
        return linkRepository
                .findByOwnerAndIsActiveTrueOrderByCreatedAtDesc(currentUserProvider.currentUid())
                .stream().map(this::toDTO).toList();
    }

    @Override
    public List<LinkDTO> listForEntity(LinkableType sourceType, String sourceId) {
        return linkRepository
                .findBySourceTypeAndSourceIdAndOwnerAndIsActiveTrue(sourceType, sourceId, currentUserProvider.currentUid())
                .stream().map(this::toDTO).toList();
    }

    @Override
    public void softDelete(String id) {
        EntityLink link = linkRepository
                .findByIdAndOwnerAndIsActiveTrue(id, currentUserProvider.currentUid())
                .orElseThrow(() -> new MicroTimeManagementNotFoundException(ErrorConstants.LINK_NOT_FOUND));
        link.setIsActive(Boolean.FALSE);
        linkRepository.save(link);
    }

    private LinkDTO toDTO(EntityLink l) {
        return LinkDTO.builder()
                .id(l.getId())
                .sourceType(l.getSourceType())
                .sourceId(l.getSourceId())
                .targetType(l.getTargetType())
                .targetId(l.getTargetId())
                .linkType(l.getLinkType())
                .createdAt(l.getCreatedAt())
                .lastUpdatedAt(l.getLastUpdatedAt())
                .isActive(l.getIsActive())
                .build();
    }
}

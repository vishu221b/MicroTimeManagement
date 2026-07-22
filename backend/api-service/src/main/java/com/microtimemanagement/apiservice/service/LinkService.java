package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.LinkDTO;
import com.microtimemanagement.apiservice.enums.LinkableType;

import java.util.List;

public interface LinkService {

    LinkDTO create(LinkDTO dto);

    List<LinkDTO> listForCurrentUser();

    List<LinkDTO> listForEntity(LinkableType sourceType, String sourceId);

    void softDelete(String id);
}

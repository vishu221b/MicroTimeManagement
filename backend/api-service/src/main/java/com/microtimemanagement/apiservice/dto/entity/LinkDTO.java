package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.enums.LinkType;
import com.microtimemanagement.apiservice.enums.LinkableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Request + response DTO for {@link com.microtimemanagement.apiservice.model.EntityLink}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkDTO {

    private String id;
    private LinkableType sourceType;
    private String sourceId;
    private LinkableType targetType;
    private String targetId;
    private LinkType linkType;
    private Date createdAt;
    private Date lastUpdatedAt;
    private Boolean isActive;
}

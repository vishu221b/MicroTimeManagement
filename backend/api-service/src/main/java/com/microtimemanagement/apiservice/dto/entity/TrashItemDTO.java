package com.microtimemanagement.apiservice.dto.entity;

import com.microtimemanagement.apiservice.enums.TrashItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * A type-agnostic row for the unified Trash / Archive view. Projects, tasks and
 * reminders are flattened into this shape so the frontend renders one list with
 * a type badge, a title, and a short subtitle for context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrashItemDTO {

    private String id;
    private TrashItemType type;
    private String title;
    private String subtitle;
    private Boolean archived;
    private Date lastUpdatedAt;
}

package com.microtimemanagement.apiservice.service;

import com.microtimemanagement.apiservice.dto.entity.TrashItemDTO;
import com.microtimemanagement.apiservice.enums.TrashItemType;

import java.util.List;

public interface TrashService {

    /** Soft-deleted items (isActive=false) across all supported types. */
    List<TrashItemDTO> listDeleted();

    /** Archived-but-active items across all supported types. */
    List<TrashItemDTO> listArchived();

    /** Move an active item into the archive. */
    void archive(TrashItemType type, String id);

    /** Restore an item to the active list — from the archive or from trash. */
    void restore(TrashItemType type, String id);

    /** Permanently delete an item from the database (no going back). */
    void purge(TrashItemType type, String id);
}

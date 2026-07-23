package com.microtimemanagement.apiservice.enums;

/**
 * The entity kinds that participate in the Trash / Archive views. Only entities
 * that soft-delete (isActive=false) and support archiving are listed here.
 */
public enum TrashItemType {
    PROJECT,
    TASK,
    REMINDER
}

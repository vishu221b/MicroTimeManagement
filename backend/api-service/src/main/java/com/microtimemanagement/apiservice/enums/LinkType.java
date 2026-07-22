package com.microtimemanagement.apiservice.enums;

/**
 * The semantic relationship a link expresses between two entities, letting
 * users wire projects/tasks/activities into a custom story or routine.
 */
public enum LinkType {
    FOLLOWS,
    PART_OF,
    BLOCKS,
    RELATES_TO
}

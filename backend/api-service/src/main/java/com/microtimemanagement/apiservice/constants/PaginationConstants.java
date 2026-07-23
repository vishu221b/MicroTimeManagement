package com.microtimemanagement.apiservice.constants;


public class PaginationConstants {
    public static final String DEFAULT_PAGE_SIZE = "50";
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_SORTING_DIRECTION = "ASC";
    // JPA entity field (was the Mongo "_id" before the Postgres migration —
    // that no longer exists on the entities and 500s the paginated finders).
    public static final String DEFAULT_FIELD_TO_SORT_BY = "id";
    public static final String MAX_PAGE_SIZE = "200";
}

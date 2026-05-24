package com.microtimemanagement.apiservice.utils;

import com.microtimemanagement.apiservice.constants.PaginationConstants;
import com.microtimemanagement.apiservice.dto.response.PaginationRequestFieldsSanitizationResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiUtils Pagination Sanitization Tests")
class ApiUtilsTest {

    private static final int DEFAULT_PAGE_NUMBER =
            Integer.parseInt(PaginationConstants.DEFAULT_PAGE_NUMBER);
    private static final int DEFAULT_PAGE_SIZE =
            Integer.parseInt(PaginationConstants.DEFAULT_PAGE_SIZE);
    private static final int MAX_PAGE_SIZE =
            Integer.parseInt(PaginationConstants.MAX_PAGE_SIZE);

    @Test
    @DisplayName("Should clamp negative pageNumber to default")
    void shouldClampNegativePageNumber() {
        PaginationRequestFieldsSanitizationResponseDTO result =
                ApiUtils.sanitizePaginationRequestFields(-5, 50);

        assertThat(result.getPageNumber()).isEqualTo(DEFAULT_PAGE_NUMBER);
        assertThat(result.getPageSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should clamp zero or negative pageSize to default page size")
    void shouldClampNonPositivePageSize() {
        PaginationRequestFieldsSanitizationResponseDTO result =
                ApiUtils.sanitizePaginationRequestFields(0, 0);

        assertThat(result.getPageSize()).isEqualTo(DEFAULT_PAGE_SIZE);
    }

    @Test
    @DisplayName("Should clamp pageSize above MAX_PAGE_SIZE")
    void shouldClampOversizedPageSize() {
        PaginationRequestFieldsSanitizationResponseDTO result =
                ApiUtils.sanitizePaginationRequestFields(0, 9999);

        assertThat(result.getPageSize()).isEqualTo(MAX_PAGE_SIZE);
    }

    @Test
    @DisplayName("Should pass valid pagination params through unchanged")
    void shouldPreserveValidParams() {
        PaginationRequestFieldsSanitizationResponseDTO result =
                ApiUtils.sanitizePaginationRequestFields(3, 75);

        assertThat(result.getPageNumber()).isEqualTo(3);
        assertThat(result.getPageSize()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should accept exactly MAX_PAGE_SIZE without clamping")
    void shouldAcceptMaxPageSizeExactly() {
        PaginationRequestFieldsSanitizationResponseDTO result =
                ApiUtils.sanitizePaginationRequestFields(0, MAX_PAGE_SIZE);

        assertThat(result.getPageSize()).isEqualTo(MAX_PAGE_SIZE);
    }
}

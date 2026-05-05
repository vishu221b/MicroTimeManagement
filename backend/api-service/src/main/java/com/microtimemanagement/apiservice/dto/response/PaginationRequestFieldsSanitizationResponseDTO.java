package com.microtimemanagement.apiservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationRequestFieldsSanitizationResponseDTO {
    private Integer pageSize;

    private Integer pageNumber;
}

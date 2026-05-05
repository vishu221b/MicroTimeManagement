package com.microtimemanagement.apiservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginationResultResponseDTO<T> {

    @JsonProperty("results")
    private List<T> payload;

    @JsonProperty("page")
    private Integer pageNumber;

    @JsonProperty("size")
    private Integer pageSize;

    @JsonProperty("totalPages")
    private Integer totalPages;

    @JsonProperty("sort")
    private String sortingDirection;

    @JsonProperty("sortedBy")
    private List<String> sortedByFields;

}

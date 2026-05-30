package com.microtimemanagement.apiservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Distinct activity names the current user has previously logged, ordered by
 * most-recent use first. Powers the create/edit form's name autocomplete so
 * users don't retype recurring activity names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityNamesResponseDTO {

    private List<String> names;
}

package com.microtimemanagement.apiservice.controller;

import com.microtimemanagement.apiservice.service.ActivityRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Routing-only regression tests for {@link ActivityRecordController}.
 *
 * <p>The create/update/delete handlers live at the controller's base path.
 * They used to be mapped via {@code EMPTY_BASE} ("/"), which combined to
 * "/api/v1/activity/" — a trailing-slash path Spring Boot 3 no longer matches
 * against the slash-less request every client sends, so POST/PUT/DELETE fell
 * through to the static-resource handler and 500'd. These tests pin the
 * base-path mappings (no trailing slash) so that never silently regresses.
 *
 * Standalone MockMvc mirrors production path matching (PathPatternParser,
 * trailing-slash matching off) without needing MongoDB or the security filter.
 */
@DisplayName("Activity Record Controller routing")
class ActivityRecordControllerRoutingTest {

    private final ActivityRecordService activityRecordService =
            Mockito.mock(ActivityRecordService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ActivityRecordController(activityRecordService))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/activity (no trailing slash) resolves to the create handler")
    void postToBasePathIsMapped() throws Exception {
        mockMvc.perform(post("/api/v1/activity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordDate\":\"2026-07-22\",\"activityName\":\"Standup\","
                                + "\"activityStartHourMinutes\":\"09:00:0\","
                                + "\"activityEndHourMinutes\":\"09:30:0\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/activity (no trailing slash) resolves to the update handler")
    void putToBasePathIsMapped() throws Exception {
        mockMvc.perform(put("/api/v1/activity")
                        .param("date", "2026-07-22")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recordId\":\"abc123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/activity (no trailing slash) resolves to the delete handler")
    void deleteToBasePathIsMapped() throws Exception {
        mockMvc.perform(delete("/api/v1/activity")
                        .param("date", "2026-07-22")
                        .param("recordId", "abc123"))
                .andExpect(status().isOk());
    }
}

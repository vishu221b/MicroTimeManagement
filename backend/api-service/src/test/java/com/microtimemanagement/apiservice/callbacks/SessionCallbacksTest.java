package com.microtimemanagement.apiservice.callbacks;

import com.microtimemanagement.apiservice.model.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Session Callbacks Tests")
class SessionCallbacksTest {

    private final SessionCallbacks callbacks = new SessionCallbacks();

    @Test
    @DisplayName("Should populate createdAt, lastUpdatedAt, and isActive on new entity")
    void shouldPopulateDefaultsOnNewEntity() {
        Session session = Session.builder().build();

        Session result = callbacks.onBeforeConvert(session, "session");

        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getLastUpdatedAt()).isNotNull();
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should NOT overwrite existing createdAt on subsequent save (e.g. logout)")
    void shouldPreserveCreatedAtOnUpdate() {
        Date originalCreatedAt = new Date(System.currentTimeMillis() - 60_000);
        Session session = Session.builder().build();
        session.setCreatedAt(originalCreatedAt);
        session.setIsActive(false);

        Session result = callbacks.onBeforeConvert(session, "session");

        assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should always refresh lastUpdatedAt on every save")
    void shouldRefreshLastUpdatedAtOnEverySave() throws InterruptedException {
        Date originalLastUpdatedAt = new Date(System.currentTimeMillis() - 60_000);
        Session session = Session.builder().build();
        session.setCreatedAt(new Date(System.currentTimeMillis() - 120_000));
        session.setLastUpdatedAt(originalLastUpdatedAt);

        Thread.sleep(5);
        Session result = callbacks.onBeforeConvert(session, "session");

        assertThat(result.getLastUpdatedAt()).isAfter(originalLastUpdatedAt);
    }

    @Test
    @DisplayName("Should default isActive to true only when null, preserving explicit false")
    void shouldPreserveExplicitIsActiveValue() {
        Session session = Session.builder().build();
        session.setIsActive(Boolean.FALSE);

        Session result = callbacks.onBeforeConvert(session, "session");

        assertThat(result.getIsActive()).isFalse();
    }
}

package com.microtimemanagement.apiservice.handler;

import com.microtimemanagement.apiservice.constants.ErrorConstants;
import com.microtimemanagement.apiservice.dto.ExceptionDTO;
import com.microtimemanagement.apiservice.exceptions.MicroTimeManagementUserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception Handler — UserException → 409 Conflict mapping")
class MicroTimeManagementResourceExceptionHandlerTest {

    private final MicroTimeManagementResourceExceptionHandler handler =
            new MicroTimeManagementResourceExceptionHandler();

    @Test
    @DisplayName("Handler method for UserException should be annotated with @ResponseStatus(CONFLICT)")
    void handlerMethodShouldBeAnnotatedWithConflictStatus() {
        Method userExceptionHandler = Arrays.stream(
                        MicroTimeManagementResourceExceptionHandler.class.getDeclaredMethods()
                )
                .filter(method -> {
                    ExceptionHandler annotation = method.getAnnotation(ExceptionHandler.class);
                    return annotation != null && Arrays.asList(annotation.value())
                            .contains(MicroTimeManagementUserException.class);
                })
                .findFirst()
                .orElse(null);

        assertThat(userExceptionHandler)
                .as("Expected an @ExceptionHandler for MicroTimeManagementUserException")
                .isNotNull();

        ResponseStatus responseStatus = userExceptionHandler.getAnnotation(ResponseStatus.class);
        assertThat(responseStatus)
                .as("Handler should declare @ResponseStatus")
                .isNotNull();
        assertThat(responseStatus.value()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Returned ExceptionDTO should carry status code 409 and the exception message")
    void shouldBuildExceptionDtoWith409AndOriginalMessage() {
        MicroTimeManagementUserException ex =
                new MicroTimeManagementUserException(ErrorConstants.USER_ALREADY_EXISTS_FOR_USERNAME);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/user/register");
        request.setServletPath("/api/v1/user/register");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ExceptionDTO<?> result = handler.handleUserConflictException(ex, webRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getPath()).isEqualTo("/api/v1/user/register");
        assertThat(result.getError()).isNotNull();
    }
}

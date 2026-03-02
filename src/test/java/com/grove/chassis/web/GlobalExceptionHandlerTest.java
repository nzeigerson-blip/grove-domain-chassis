package com.grove.chassis.web;

import com.grove.chassis.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Request-ID")).thenReturn("req-123");
    }

    @Test
    @DisplayName("should_Return404WithErrorEnvelope_When_ResourceNotFound")
    void should_Return404WithErrorEnvelope_When_ResourceNotFound() {
        var ex = new ResourceNotFoundException("Account", "abc-123") {};
        var response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().error().code()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().error().requestId()).isEqualTo("req-123");
        assertThat(response.getBody().error().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("should_Return422WithErrorCode_When_BusinessRuleViolated")
    void should_Return422WithErrorCode_When_BusinessRuleViolated() {
        var ex = new BusinessRuleException("INSUFFICIENT_FUNDS", "Not enough money");
        var response = handler.handleBusinessRule(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody().error().code()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(response.getBody().error().message()).isEqualTo("Not enough money");
    }

    @Test
    @DisplayName("should_Return409_When_ConflictOccurs")
    void should_Return409_When_ConflictOccurs() {
        var ex = new ConflictException("DUPLICATE_ENTITY", "Already exists");
        var response = handler.handleConflict(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().error().code()).isEqualTo("DUPLICATE_ENTITY");
    }

    @Test
    @DisplayName("should_Return400WithFieldDetails_When_ValidationFails")
    void should_Return400WithFieldDetails_When_ValidationFails() {
        var ex = new ValidationException("Bad input", List.of("name: required", "email: invalid format"));
        var response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().error().details()).hasSize(2);
        assertThat(response.getBody().error().details()).contains("name: required");
    }

    @Test
    @DisplayName("should_Return500WithoutInternalDetails_When_UnexpectedError")
    void should_Return500WithoutInternalDetails_When_UnexpectedError() {
        var ex = new RuntimeException("NullPointerException in PaymentProcessor");
        var response = handler.handleUnexpected(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_ERROR");
        // Must NOT leak internal error details to client
        assertThat(response.getBody().error().message()).doesNotContain("NullPointerException");
        assertThat(response.getBody().error().message()).doesNotContain("PaymentProcessor");
    }

    @Test
    @DisplayName("should_UseUnknownRequestId_When_HeaderMissing")
    void should_UseUnknownRequestId_When_HeaderMissing() {
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        var ex = new BusinessRuleException("TEST", "test");
        var response = handler.handleBusinessRule(ex, request);

        assertThat(response.getBody().error().requestId()).isEqualTo("unknown");
    }
}

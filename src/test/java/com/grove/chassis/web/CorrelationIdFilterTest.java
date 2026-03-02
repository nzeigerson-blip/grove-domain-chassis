package com.grove.chassis.web;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    @DisplayName("should_EchoRequestIdAndSetCorrelationId_When_RequestProcessed")
    void should_EchoRequestIdAndSetCorrelationId_When_RequestProcessed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-ID", "client-req-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Request-ID")).isEqualTo("client-req-1");
        assertThat(response.getHeader("X-Correlation-ID")).isNotNull().isNotBlank();
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("should_GenerateRequestId_When_ClientOmitsHeader")
    void should_GenerateRequestId_When_ClientOmitsHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Request-ID")).isNotNull().isNotBlank();
        assertThat(response.getHeader("X-Correlation-ID")).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("should_PopulateMDCDuringRequest_When_FilterActive")
    void should_PopulateMDCDuringRequest_When_FilterActive() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            assertThat(MDC.get("correlationId")).isNotNull();
            assertThat(MDC.get("requestId")).isNotNull();
        };

        filter.doFilterInternal(request, response, chain);
    }

    @Test
    @DisplayName("should_ClearMDC_When_RequestCompleted")
    void should_ClearMDC_When_RequestCompleted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("requestId")).isNull();
    }
}

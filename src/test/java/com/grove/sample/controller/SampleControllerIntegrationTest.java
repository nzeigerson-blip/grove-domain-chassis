package com.grove.sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grove.sample.model.dto.SampleRequest;
import com.grove.sample.model.dto.SampleResponse;
import com.grove.sample.service.SampleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SampleController.class)
class SampleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SampleService sampleService;

    @Test
    @DisplayName("should_Return201WithStandardEnvelope_When_ValidCreateRequest")
    @WithMockUser
    void should_Return201WithStandardEnvelope_When_ValidCreateRequest() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        SampleResponse response = new SampleResponse(id, "Test", "Desc", now, now);

        when(sampleService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/samples")
                        .header("X-Request-ID", "req-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SampleRequest("Test", "Desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.name").value("Test"))
                .andExpect(jsonPath("$.meta.requestId").value("req-1"))
                .andExpect(jsonPath("$.meta.timestamp").exists());
    }

    @Test
    @DisplayName("should_Return401_When_NoAuthentication")
    void should_Return401_When_NoAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/samples/" + UUID.randomUUID())
                        .header("X-Request-ID", "req-2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should_Return400WithValidationDetails_When_BlankFields")
    @WithMockUser
    void should_Return400WithValidationDetails_When_BlankFields() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                        .header("X-Request-ID", "req-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    @DisplayName("should_Return200WithData_When_AuthenticatedGetRequest")
    @WithMockUser
    void should_Return200WithData_When_AuthenticatedGetRequest() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        when(sampleService.getById(id)).thenReturn(new SampleResponse(id, "Item", "Desc", now, now));

        mockMvc.perform(get("/api/v1/samples/" + id)
                        .header("X-Request-ID", "req-4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.meta.requestId").value("req-4"));
    }

    @Test
    @DisplayName("should_Return204_When_SoftDeleteSucceeds")
    @WithMockUser
    void should_Return204_When_SoftDeleteSucceeds() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/samples/" + id))
                .andExpect(status().isNoContent());
    }
}

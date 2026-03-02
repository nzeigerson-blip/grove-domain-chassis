package com.grove.sample.controller;

import com.grove.chassis.web.ApiResponse;
import com.grove.sample.model.dto.SampleRequest;
import com.grove.sample.model.dto.SampleResponse;
import com.grove.sample.service.SampleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * SAMPLE: REST controller. Thin — delegates to service.
 * Shows the pattern: request validation, standard envelope, required headers, soft-delete.
 */
@RestController
@RequestMapping("/api/v1/samples")
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SampleResponse>> create(
            @RequestHeader("X-Request-ID") String requestId,
            @Valid @RequestBody SampleRequest request) {
        SampleResponse response = sampleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(response, requestId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SampleResponse>> getById(
            @RequestHeader("X-Request-ID") String requestId,
            @PathVariable UUID id) {
        SampleResponse response = sampleService.getById(id);
        return ResponseEntity.ok(ApiResponse.of(response, requestId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sampleService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.grove.sample.service;

import com.grove.chassis.event.EventPublisher;
import com.grove.sample.exception.SampleNotFoundException;
import com.grove.sample.model.dto.SampleRequest;
import com.grove.sample.model.dto.SampleResponse;
import com.grove.sample.model.entity.SampleEntity;
import com.grove.sample.model.event.SampleCreatedEvent;
import com.grove.sample.repository.SampleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * SAMPLE: Business logic service. Shows the pattern:
 * - Constructor injection (all deps final)
 * - Publishes rich events after state changes
 * - Soft-delete only
 * - Maps entity → response DTO
 */
@Slf4j
@Service
public class SampleService {

    private static final String TOPIC = "sample-item-created";
    private static final String EVENT_TYPE = "sample.item.created";

    private final SampleRepository sampleRepository;
    private final EventPublisher eventPublisher;

    public SampleService(SampleRepository sampleRepository, EventPublisher eventPublisher) {
        this.sampleRepository = sampleRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SampleResponse create(SampleRequest request) {
        SampleEntity entity = new SampleEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());

        SampleEntity saved = sampleRepository.save(entity);
        log.info("Sample item created: id={}", saved.getId());

        // Publish rich event with ALL domain data
        SampleCreatedEvent event = new SampleCreatedEvent(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getCreatedAt()
        );
        eventPublisher.publish(TOPIC, saved.getId().toString(), EVENT_TYPE, event);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SampleResponse getById(UUID id) {
        SampleEntity entity = sampleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new SampleNotFoundException(id.toString()));
        return toResponse(entity);
    }

    @Transactional
    public void softDelete(UUID id) {
        SampleEntity entity = sampleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new SampleNotFoundException(id.toString()));
        entity.setDeleted(true);
        sampleRepository.save(entity);
        log.info("Sample item soft-deleted: id={}", id);
    }

    private SampleResponse toResponse(SampleEntity entity) {
        return new SampleResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

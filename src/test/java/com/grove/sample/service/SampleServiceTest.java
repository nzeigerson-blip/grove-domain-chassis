package com.grove.sample.service;

import com.grove.chassis.event.EventPublisher;
import com.grove.sample.exception.SampleNotFoundException;
import com.grove.sample.model.dto.SampleRequest;
import com.grove.sample.model.dto.SampleResponse;
import com.grove.sample.model.entity.SampleEntity;
import com.grove.sample.repository.SampleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private SampleService sampleService;

    @Test
    @DisplayName("should_CreateEntityAndPublishRichEvent_When_ValidRequest")
    void should_CreateEntityAndPublishRichEvent_When_ValidRequest() {
        SampleRequest request = new SampleRequest("Test Item", "A description");
        SampleEntity saved = createEntity(UUID.randomUUID(), "Test Item", "A description");

        when(sampleRepository.save(any())).thenReturn(saved);
        when(eventPublisher.publish(any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        SampleResponse response = sampleService.create(request);

        assertThat(response.name()).isEqualTo("Test Item");
        assertThat(response.description()).isEqualTo("A description");
        assertThat(response.id()).isNotNull();
        verify(eventPublisher).publish(eq("sample-item-created"), any(), eq("sample.item.created"), any());
    }

    @Test
    @DisplayName("should_ReturnResponse_When_EntityExistsAndNotDeleted")
    void should_ReturnResponse_When_EntityExistsAndNotDeleted() {
        UUID id = UUID.randomUUID();
        SampleEntity entity = createEntity(id, "Found", "Exists");

        when(sampleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        SampleResponse response = sampleService.getById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Found");
    }

    @Test
    @DisplayName("should_ThrowSampleNotFound_When_EntityDoesNotExist")
    void should_ThrowSampleNotFound_When_EntityDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(sampleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sampleService.getById(id))
                .isInstanceOf(SampleNotFoundException.class);
    }

    @Test
    @DisplayName("should_SetDeletedFlag_When_SoftDeleting")
    void should_SetDeletedFlag_When_SoftDeleting() {
        UUID id = UUID.randomUUID();
        SampleEntity entity = createEntity(id, "ToDelete", "Delete me");

        when(sampleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(sampleRepository.save(any())).thenReturn(entity);

        sampleService.softDelete(id);

        verify(sampleRepository).save(entity);
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("should_ThrowSampleNotFound_When_SoftDeletingNonExistent")
    void should_ThrowSampleNotFound_When_SoftDeletingNonExistent() {
        UUID id = UUID.randomUUID();
        when(sampleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sampleService.softDelete(id))
                .isInstanceOf(SampleNotFoundException.class);
    }

    private SampleEntity createEntity(UUID id, String name, String description) {
        SampleEntity entity = new SampleEntity();
        entity.setName(name);
        entity.setDescription(description);
        try {
            var idField = SampleEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
            var createdAtField = SampleEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, Instant.now());
            var updatedAtField = SampleEntity.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entity;
    }
}

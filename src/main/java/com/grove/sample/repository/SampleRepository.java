package com.grove.sample.repository;

import com.grove.sample.model.entity.SampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * SAMPLE: Repository with soft-delete-aware query.
 */
@Repository
public interface SampleRepository extends JpaRepository<SampleEntity, UUID> {

    Optional<SampleEntity> findByIdAndDeletedFalse(UUID id);
}

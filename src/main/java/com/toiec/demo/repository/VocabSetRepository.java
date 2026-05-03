package com.toiec.demo.repository;

import com.toiec.demo.entities.VocabSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;

public interface VocabSetRepository extends JpaRepository<VocabSet, UUID>, JpaSpecificationExecutor<VocabSet> {
    Page<VocabSet> findByIsPublicTrue(Pageable pageable);

    // Thêm method này để tìm bộ từ public theo topic (không phân biệt hoa thường)
    Page<VocabSet> findByIsPublicTrueAndTopicContainingIgnoreCase(String topic, Pageable pageable);

    Page<VocabSet> findByCreatedById(UUID userId, Pageable pageable);
    Optional<VocabSet> findByIdAndCreatedById(UUID id, UUID userId);
    long countByCreatedById(UUID userId);
}
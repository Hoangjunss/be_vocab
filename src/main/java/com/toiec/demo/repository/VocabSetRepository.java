package com.toiec.demo.repository;

import com.toiec.demo.entities.VocabSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface VocabSetRepository extends JpaRepository<VocabSet, String>, JpaSpecificationExecutor<VocabSet> {
    Page<VocabSet> findByIsPublicTrue(Pageable pageable);

    // Thêm method này để tìm bộ từ public theo topic (không phân biệt hoa thường)
    Page<VocabSet> findByIsPublicTrueAndTopicContainingIgnoreCase(String topic, Pageable pageable);

    Page<VocabSet> findByCreatedById(String userId, Pageable pageable);
    Optional<VocabSet> findByIdAndCreatedById(String id, String userId);
    long countByCreatedById(String userId);
}
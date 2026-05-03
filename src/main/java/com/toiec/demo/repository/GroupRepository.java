package com.toiec.demo.repository;

import com.toiec.demo.entities.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Page<Group> findByIsPublicTrue(Pageable pageable);
    Page<Group> findByCreatedBy(UUID userId, Pageable pageable);
    Optional<Group> findByIdAndCreatedBy(UUID id, UUID userId);
}
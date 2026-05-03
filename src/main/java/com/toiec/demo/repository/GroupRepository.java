package com.toiec.demo.repository;

import com.toiec.demo.entities.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, String> {
    Page<Group> findByIsPublicTrue(Pageable pageable);
    Page<Group> findByCreatedBy(String userId, Pageable pageable);
    Optional<Group> findByIdAndCreatedBy(String id, String userId);
}
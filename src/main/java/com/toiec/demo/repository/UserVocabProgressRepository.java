package com.toiec.demo.repository;

import com.toiec.demo.entities.UserVocabProgress;
import com.toiec.demo.entities.UserVocabProgressId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UserVocabProgressRepository extends JpaRepository<UserVocabProgress, UserVocabProgressId> {
    Optional<UserVocabProgress> findByUserIdAndCardId(String userId, String cardId);
    List<UserVocabProgress> findByUserIdAndNextReviewAtBefore(String userId, OffsetDateTime now, Pageable pageable);
    long countByUserIdAndSrsLevelGreaterThanEqual(String userId, int level);

    @Modifying
    @Transactional
    @Query("UPDATE UserVocabProgress p SET p.srsLevel = p.srsLevel + 1 WHERE p.userId = :userId AND p.cardId = :cardId")
    void incrementSrsLevel(String userId, String cardId);
}
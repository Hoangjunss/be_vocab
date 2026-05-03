package com.toiec.demo.repository;


import com.toiec.demo.entities.UserVocabProgress;
import com.toiec.demo.entities.UserVocabProgressId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserVocabProgressRepository extends JpaRepository<UserVocabProgress, UserVocabProgressId> {

    @Query("SELECT p FROM UserVocabProgress p WHERE p.id.userId = :userId AND p.id.cardId = :cardId")
    Optional<UserVocabProgress> findByUserIdAndCardId(@Param("userId") UUID userId, @Param("cardId") UUID cardId);

    @Query("SELECT p FROM UserVocabProgress p WHERE p.id.userId = :userId AND p.nextReviewAt <= :now")
    List<UserVocabProgress> findByUserIdAndNextReviewAtBefore(@Param("userId") UUID userId, @Param("now") OffsetDateTime now, Pageable pageable);

    @Query("SELECT COUNT(p) FROM UserVocabProgress p WHERE p.id.userId = :userId AND p.nextReviewAt <= :now")
    long countByUserIdAndNextReviewAtBefore(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    @Query("SELECT COUNT(p) FROM UserVocabProgress p WHERE p.id.userId = :userId AND p.srsLevel >= :level")
    long countByUserIdAndSrsLevelGreaterThanEqual(@Param("userId") UUID userId, @Param("level") int level);

    @Modifying
    @Transactional
    @Query("UPDATE UserVocabProgress p SET p.srsLevel = p.srsLevel + 1 WHERE p.id.userId = :userId AND p.id.cardId = :cardId")
    void incrementSrsLevel(@Param("userId") UUID userId, @Param("cardId") UUID cardId);
    @Query("SELECT p.id.cardId FROM UserVocabProgress p WHERE p.id.userId = :userId AND p.srsLevel >= 1")
    Set<UUID> findLearnedCardIdsByUserId(@Param("userId") UUID userId);
}
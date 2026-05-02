package com.toiec.demo.repository;

import com.toiec.demo.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    @Modifying
    @Transactional
    @Query("UPDATE UserProfile p SET p.xp = p.xp + :xp, p.totalFlashcardReviews = p.totalFlashcardReviews + 1, p.updatedAt = CURRENT_TIMESTAMP WHERE p.userId = :userId")
    void addXpAndReview(String userId, int xp);

    @Modifying
    @Transactional
    @Query("UPDATE UserProfile p SET p.totalWordsLearned = p.totalWordsLearned + 1 WHERE p.userId = :userId")
    void incrementTotalWordsLearned(String userId);
}
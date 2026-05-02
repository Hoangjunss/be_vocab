package com.toiec.demo.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private int xp = 0;
    private int level = 1;

    @Column(name = "current_streak")
    private int currentStreak = 0;

    @Column(name = "max_streak")
    private int maxStreak = 0;

    @Column(name = "total_words_learned")
    private int totalWordsLearned = 0;

    @Column(name = "total_flashcard_reviews")
    private int totalFlashcardReviews = 0;

    @Column(name = "study_time_seconds")
    private int studyTimeSeconds = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
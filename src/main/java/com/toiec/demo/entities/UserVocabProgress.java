package com.toiec.demo.entities;


import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_vocab_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVocabProgress {
    @EmbeddedId
    private UserVocabProgressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cardId")
    @JoinColumn(name = "card_id")
    private VocabCard card;

    @Column(name = "srs_level")
    private Short srsLevel = 0;

    @Column(name = "next_review_at")
    private OffsetDateTime nextReviewAt;

    @Column(name = "times_reviewed")
    private Integer timesReviewed = 0;

    @Column(name = "streak_correct")
    private Short streakCorrect = 0;

    @Column(name = "ease_factor")
    private Double easeFactor = 2.5;

    @Column(name = "last_quality")
    private Short lastQuality;

    @Column(name = "last_reviewed_at")
    private OffsetDateTime lastReviewedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
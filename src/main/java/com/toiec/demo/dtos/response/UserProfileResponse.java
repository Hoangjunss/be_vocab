package com.toiec.demo.dtos.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private String id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private int xp;
    private int level;
    private int currentStreak;
    private int maxStreak;
    private int totalWordsLearned;
    private int totalFlashcardReviews;
}
package com.toiec.demo.dtos.response;


import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class VocabSetResponse {
    private String id;
    private String title;
    private String description;
    private boolean isPublic;
    private String createdBy;   // user id or null
    private String createdByName;
    private String topic;
    private Integer difficultyLevel;
    private Integer wordCount;
    private String imageUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<VocabCardResponse> cards;
}
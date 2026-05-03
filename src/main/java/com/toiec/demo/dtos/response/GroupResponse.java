package com.toiec.demo.dtos.response;


import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class GroupResponse {
    private String id;
    private String name;
    private String description;
    private boolean isPublic;
    private String createdBy;
    private String createdByName;  // full name of creator
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<VocabSetResponse> vocabSets;  // optional
    private Integer vocabSetCount;
}
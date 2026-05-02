package com.toiec.demo.dtos.request;


import lombok.Data;

@Data
public class UpdateVocabSetRequest {
    private String title;
    private String description;
    private Boolean isPublic;
    private String topic;
    private Integer difficultyLevel;
    private String imageUrl;
}
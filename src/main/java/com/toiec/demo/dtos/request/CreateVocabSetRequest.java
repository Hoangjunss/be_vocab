package com.toiec.demo.dtos.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVocabSetRequest {
    @NotBlank
    private String title;
    private String description;
    private Boolean isPublic = false;
    private String topic;
    private Integer difficultyLevel = 1;
    private String imageUrl;
}
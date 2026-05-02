package com.toiec.demo.dtos.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateVocabCardRequest {
    @NotBlank
    private String word;
    @NotBlank
    private String meaning;
    private String exampleSentence;
    private String phonetic;
    private String audioUrl;
    private String imageUrl;
    private Double difficultyRating;
    private List<String> tags;
}
package com.toiec.demo.dtos.request;


import lombok.Data;
import java.util.List;

@Data
public class UpdateVocabCardRequest {
    private String word;
    private String meaning;
    private String exampleSentence;
    private String phonetic;
    private String audioUrl;
    private String imageUrl;
    private Double difficultyRating;
    private List<String> tags;
}
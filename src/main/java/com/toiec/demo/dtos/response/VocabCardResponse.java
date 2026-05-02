package com.toiec.demo.dtos.response;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class VocabCardResponse {
    private String id;
    private String word;
    private String meaning;
    private String exampleSentence;
    private String phonetic;
    private String audioUrl;
    private String imageUrl;
    private Double difficultyRating;
    private List<String> tags;
}
package com.toiec.demo.dtos.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlashcardSessionResponse {
    private String cardId;
    private String word;
    private String meaning;
    private String exampleSentence;
    private String phonetic;
    private String audioUrl;
    private String imageUrl;
    private int currentSrsLevel;
    private int totalCardsInSet;
    private int reviewedCount;
    private int cardsDueToday;
}
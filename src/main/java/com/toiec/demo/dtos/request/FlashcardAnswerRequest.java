package com.toiec.demo.dtos.request;



import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FlashcardAnswerRequest {
    @NotBlank
    private String cardId;
    @Min(0) @Max(4)
    private int quality;   // 0=black,4=easy
}
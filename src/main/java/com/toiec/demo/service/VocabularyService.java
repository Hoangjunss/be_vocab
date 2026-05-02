package com.toiec.demo.service;

import com.toiec.demo.dtos.request.*;
import com.toiec.demo.dtos.response.FlashcardSessionResponse;
import com.toiec.demo.dtos.response.VocabCardResponse;
import com.toiec.demo.dtos.response.VocabSetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface VocabularyService {
    // Set operations
    VocabSetResponse createSet(CreateVocabSetRequest request, String userId);
    VocabSetResponse updateSet(String setId, UpdateVocabSetRequest request, String userId);
    void deleteSet(String setId, String userId);
    VocabSetResponse getSetById(String setId, String userId);
    Page<VocabSetResponse> getPublicSets(String topic, Pageable pageable);
    Page<VocabSetResponse> getUserSets(String userId, Pageable pageable);

    // Card operations
    VocabCardResponse addCard(String setId, CreateVocabCardRequest request, String userId);
    VocabCardResponse updateCard(String cardId, UpdateVocabCardRequest request, String userId);
    void deleteCard(String cardId, String userId);
    List<VocabCardResponse> getCardsBySet(String setId, String userId);

    // Bulk import
    void importCardsFromFile(String setId, MultipartFile file, String userId);

    // Flashcard learning
    FlashcardSessionResponse getNextFlashcard(String userId, String setId);
    void submitAnswer(String userId, FlashcardAnswerRequest request);
}
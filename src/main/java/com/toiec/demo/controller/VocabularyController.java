package com.toiec.demo.controller;

import com.toiec.demo.annotation.RateLimit;
import com.toiec.demo.dtos.request.*;
import com.toiec.demo.dtos.response.ApiResponse;
import com.toiec.demo.dtos.response.FlashcardSessionResponse;
import com.toiec.demo.dtos.response.VocabCardResponse;
import com.toiec.demo.dtos.response.VocabSetResponse;
import com.toiec.demo.security.CurrentUser;
import com.toiec.demo.security.UserPrincipal;
import com.toiec.demo.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/vocab")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class VocabularyController {
    private final VocabularyService vocabularyService;

    // === Set operations ===
    @PostMapping("/sets")
    @RateLimit(name = "createSet", capacity = 20, refillTokens = 10, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<VocabSetResponse>> createSet(@Valid @RequestBody CreateVocabSetRequest request,
                                                                   @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.createSet(request, currentUser.getId())));
    }

    @PutMapping("/sets/{setId}")
    @RateLimit(name = "updateSet", capacity = 20, refillTokens = 10, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<VocabSetResponse>> updateSet(@PathVariable String setId,
                                                                   @Valid @RequestBody UpdateVocabSetRequest request,
                                                                   @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.updateSet(setId, request, currentUser.getId())));
    }

    @DeleteMapping("/sets/{setId}")
    @RateLimit(name = "deleteSet", capacity = 10, refillTokens = 5, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Void>> deleteSet(@PathVariable String setId,
                                                       @CurrentUser UserPrincipal currentUser) {
        vocabularyService.deleteSet(setId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/sets/{setId}")
    @RateLimit(name = "getSet", capacity = 100, refillTokens = 50, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<VocabSetResponse>> getSet(@PathVariable String setId,
                                                                @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getSetById(setId, currentUser.getId())));
    }

    @GetMapping("/sets/public")
    @RateLimit(name = "publicSets", capacity = 100, refillTokens = 50, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Page<VocabSetResponse>>> getPublicSets(@RequestParam(required = false) String topic,
                                                                             Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getPublicSets(topic, pageable)));
    }

    @GetMapping("/sets/my")
    @RateLimit(name = "mySets", capacity = 80, refillTokens = 40, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Page<VocabSetResponse>>> getMySets(@CurrentUser UserPrincipal currentUser,
                                                                         Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getUserSets(currentUser.getId(), pageable)));
    }

    // === Card operations ===
    @PostMapping("/sets/{setId}/cards")
    @RateLimit(name = "addCard", capacity = 30, refillTokens = 15, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<VocabCardResponse>> addCard(@PathVariable String setId,
                                                                  @Valid @RequestBody CreateVocabCardRequest request,
                                                                  @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.addCard(setId, request, currentUser.getId())));
    }

    @PutMapping("/cards/{cardId}")
    @RateLimit(name = "updateCard", capacity = 30, refillTokens = 15, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<VocabCardResponse>> updateCard(@PathVariable String cardId,
                                                                     @Valid @RequestBody UpdateVocabCardRequest request,
                                                                     @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.updateCard(cardId, request, currentUser.getId())));
    }

    @DeleteMapping("/cards/{cardId}")
    @RateLimit(name = "deleteCard", capacity = 15, refillTokens = 8, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Void>> deleteCard(@PathVariable String cardId,
                                                        @CurrentUser UserPrincipal currentUser) {
        vocabularyService.deleteCard(cardId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/sets/{setId}/cards")
    @RateLimit(name = "getCards", capacity = 100, refillTokens = 50, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Page<VocabCardResponse>>> getCardsBySet(
            @PathVariable String setId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser UserPrincipal currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        UUID userId = (currentUser != null) ? UUID.fromString(currentUser.getId()) : null;
        Page<VocabCardResponse> result = vocabularyService.getCardsBySet(UUID.fromString(setId), pageable, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // === Import ===
    @PostMapping("/sets/{setId}/import")
    @RateLimit(name = "importCards", capacity = 10, refillTokens = 5, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Void>> importCards(@PathVariable String setId,
                                                         @RequestParam("file") MultipartFile file,
                                                         @CurrentUser UserPrincipal currentUser) {
        vocabularyService.importCardsFromFile(setId, file, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Imported successfully", null));
    }

    // === Flashcard study ===
    @GetMapping("/study/{setId}/next")
    @RateLimit(name = "nextFlashcard", capacity = 60, refillTokens = 30, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<FlashcardSessionResponse>> getNextFlashcard(@PathVariable String setId,
                                                                                  @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getNextFlashcard(currentUser.getId(), setId)));
    }

    @PostMapping("/study/answer")
    @RateLimit(name = "submitAnswer", capacity = 60, refillTokens = 30, refillPeriodMinutes = 1)
    public ResponseEntity<ApiResponse<Void>> submitAnswer(@Valid @RequestBody FlashcardAnswerRequest request,
                                                          @CurrentUser UserPrincipal currentUser) {
        vocabularyService.submitAnswer(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
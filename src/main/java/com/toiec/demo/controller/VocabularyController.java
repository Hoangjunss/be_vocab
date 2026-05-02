package com.toiec.demo.controller;


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
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/vocab")
@RequiredArgsConstructor
public class VocabularyController {
    private final VocabularyService vocabularyService;

    // Sets
    @PostMapping("/sets")
    public ResponseEntity<ApiResponse<VocabSetResponse>> createSet(@Valid @RequestBody CreateVocabSetRequest request,
                                                                   @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.createSet(request, currentUser.getId())));
    }

    @PutMapping("/sets/{setId}")
    public ResponseEntity<ApiResponse<VocabSetResponse>> updateSet(@PathVariable String setId,
                                                                   @Valid @RequestBody UpdateVocabSetRequest request,
                                                                   @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.updateSet(setId, request, currentUser.getId())));
    }

    @DeleteMapping("/sets/{setId}")
    public ResponseEntity<ApiResponse<Void>> deleteSet(@PathVariable String setId,
                                                       @CurrentUser UserPrincipal currentUser) {
        vocabularyService.deleteSet(setId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/sets/{setId}")
    public ResponseEntity<ApiResponse<VocabSetResponse>> getSet(@PathVariable String setId,
                                                                @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getSetById(setId, currentUser.getId())));
    }

    @GetMapping("/sets/public")
    public ResponseEntity<ApiResponse<Page<VocabSetResponse>>> getPublicSets(@RequestParam(required = false) String topic,
                                                                             Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getPublicSets(topic, pageable)));
    }

    @GetMapping("/sets/my")
    public ResponseEntity<ApiResponse<Page<VocabSetResponse>>> getMySets(@CurrentUser UserPrincipal currentUser,
                                                                         Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getUserSets(currentUser.getId(), pageable)));
    }

    // Cards
    @PostMapping("/sets/{setId}/cards")
    public ResponseEntity<ApiResponse<VocabCardResponse>> addCard(@PathVariable String setId,
                                                                  @Valid @RequestBody CreateVocabCardRequest request,
                                                                  @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.addCard(setId, request, currentUser.getId())));
    }

    @PutMapping("/cards/{cardId}")
    public ResponseEntity<ApiResponse<VocabCardResponse>> updateCard(@PathVariable String cardId,
                                                                     @Valid @RequestBody UpdateVocabCardRequest request,
                                                                     @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.updateCard(cardId, request, currentUser.getId())));
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(@PathVariable String cardId,
                                                        @CurrentUser UserPrincipal currentUser) {
        vocabularyService.deleteCard(cardId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/sets/{setId}/cards")
    public ResponseEntity<ApiResponse<List<VocabCardResponse>>> getCards(@PathVariable String setId,
                                                                         @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getCardsBySet(setId, currentUser.getId())));
    }

    // Import
    @PostMapping("/sets/{setId}/import")
    public ResponseEntity<ApiResponse<Void>> importCards(@PathVariable String setId,
                                                         @RequestParam("file") MultipartFile file,
                                                         @CurrentUser UserPrincipal currentUser) {
        vocabularyService.importCardsFromFile(setId, file, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Imported successfully", null));
    }

    // Flashcard study
    @GetMapping("/study/{setId}/next")
    public ResponseEntity<ApiResponse<FlashcardSessionResponse>> getNextFlashcard(@PathVariable String setId,
                                                                                  @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(vocabularyService.getNextFlashcard(currentUser.getId(), setId)));
    }

    @PostMapping("/study/answer")
    public ResponseEntity<ApiResponse<Void>> submitAnswer(@Valid @RequestBody FlashcardAnswerRequest request,
                                                          @CurrentUser UserPrincipal currentUser) {
        vocabularyService.submitAnswer(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
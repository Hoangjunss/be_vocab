package com.toiec.demo.service;

import com.toiec.demo.dtos.request.*;
import com.toiec.demo.dtos.response.FlashcardSessionResponse;
import com.toiec.demo.dtos.response.VocabCardResponse;
import com.toiec.demo.dtos.response.VocabSetResponse;
import com.toiec.demo.entities.*;
import com.toiec.demo.exception.BusinessRuleException;
import com.toiec.demo.exception.ResourceNotFoundException;
import com.toiec.demo.mapper.VocabCardMapper;
import com.toiec.demo.mapper.VocabSetMapper;
import com.toiec.demo.policy.SrsPolicy;
import com.toiec.demo.policy.XpPolicy;
import com.toiec.demo.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyServiceImpl implements VocabularyService {
    private final VocabSetRepository vocabSetRepository;
    private final VocabCardRepository vocabCardRepository;
    private final UserVocabProgressRepository progressRepository;
    private final UserProfileRepository profileRepository;
    private final GroupRepository groupRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final VocabSetMapper vocabSetMapper;
    private final VocabCardMapper vocabCardMapper;
    @PersistenceContext
    private EntityManager entityManager;

    // ========== Set operations ==========
    @Override
    @Transactional
    public VocabSetResponse createSet(CreateVocabSetRequest request, String userId) {
        VocabSet set = vocabSetMapper.toEntity(request);
        // Lấy reference (proxy) của User để tránh query không cần thiết
        User userRef = entityManager.getReference(User.class, userId);
        set.setCreatedBy(userRef);
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(UUID.fromString(request.getGroupId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
            set.setGroup(group);
        }
        set = vocabSetRepository.save(set);
        return vocabSetMapper.toResponse(set);
    }

    @Override
    @Transactional
    public VocabSetResponse updateSet(String setId, UpdateVocabSetRequest request, String userId) {
        VocabSet set = findSetByIdAndUser(setId, userId);
        vocabSetMapper.updateEntity(request, set);
        set = vocabSetRepository.save(set);
        return vocabSetMapper.toResponse(set);
    }

    @Override
    @Transactional
    public void deleteSet(String setId, String userId) {
        VocabSet set = findSetByIdAndUser(setId, userId);
        vocabCardRepository.deleteAllByVocabSetId(UUID.fromString(setId));
        vocabSetRepository.delete(set);
    }

    @Override
    public VocabSetResponse getSetById(String setId, String userId) {
        VocabSet set = vocabSetRepository.findById(UUID.fromString(setId))
                .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
        if (!set.isPublic() && !set.getCreatedBy().getId().equals(userId)) {
            throw new BusinessRuleException("You don't have permission to view this set");
        }
        VocabSetResponse response = vocabSetMapper.toResponse(set);
        response.setCards(set.getCards().stream().map(vocabCardMapper::toResponse).collect(Collectors.toList()));
        return response;
    }

    @Override
    public Page<VocabSetResponse> getPublicSets(String topic, Pageable pageable) {
        Page<VocabSet> sets;
        if (topic != null && !topic.isEmpty()) {
            sets = vocabSetRepository.findByIsPublicTrueAndTopicContainingIgnoreCase(topic, pageable); // need to add method
        } else {
            sets = vocabSetRepository.findByIsPublicTrue(pageable);
        }
        return sets.map(vocabSetMapper::toResponse);
    }

    @Override
    public Page<VocabSetResponse> getUserSets(String userId, Pageable pageable) {
        return vocabSetRepository.findByCreatedById(UUID.fromString(userId), pageable)
                .map(vocabSetMapper::toResponse);
    }

    // ========== Card operations ==========
    @Override
    @Transactional
    public VocabCardResponse addCard(String setId, CreateVocabCardRequest request, String userId) {
        VocabSet set = findSetByIdAndUser(setId, userId);
        VocabCard card = vocabCardMapper.toEntity(request);
        card.setVocabSet(set);
        set.getCards().add(card);
        set.setWordCount(set.getCards().size());
        vocabSetRepository.save(set);
        return vocabCardMapper.toResponse(card);
    }

    @Override
    @Transactional
    public VocabCardResponse updateCard(String cardId, UpdateVocabCardRequest request, String userId) {
        VocabCard card = findCardByIdAndOwnership(cardId, userId);
        vocabCardMapper.updateEntity(request, card);
        card = vocabCardRepository.save(card);
        return vocabCardMapper.toResponse(card);
    }

    @Override
    @Transactional
    public void deleteCard(String cardId, String userId) {
        VocabCard card = findCardByIdAndOwnership(cardId, userId);
        VocabSet set = card.getVocabSet();
        set.getCards().remove(card);
        set.setWordCount(set.getCards().size());
        vocabSetRepository.save(set);
    }

    @Override
    public List<VocabCardResponse> getCardsBySet(String setId, String userId) {
        VocabSet set = findSetByIdAndUser(setId, userId);
        return set.getCards().stream().map(vocabCardMapper::toResponse).collect(Collectors.toList());
    }

    // ========== Bulk import ==========
    @Override
    @Transactional
    public void importCardsFromFile(String setId, MultipartFile file, String userId) {
        VocabSet set = findSetByIdAndUser(setId, userId);
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            List<VocabCard> newCards = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String word = row.getCell(0).getStringCellValue();
                String meaning = row.getCell(1).getStringCellValue();
                String example = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;
                VocabCard card = VocabCard.builder()
                        .word(word).meaning(meaning).exampleSentence(example).vocabSet(set).build();
                newCards.add(card);
            }
            set.getCards().addAll(newCards);
            set.setWordCount(set.getCards().size());
            vocabSetRepository.save(set);
            log.info("Imported {} cards into set {}", newCards.size(), setId);
        } catch (Exception e) {
            throw new BusinessRuleException("Failed to parse file: " + e.getMessage());
        }
    }

    // ========== Flashcard learning ==========
    @Override
    public FlashcardSessionResponse getNextFlashcard(String userId, String setId) {
        VocabSet set = vocabSetRepository.findById(UUID.fromString(setId))
                .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
        // Get due cards for this user from this set
        List<UserVocabProgress> dueProgress = progressRepository.findByUserIdAndNextReviewAtBefore(UUID.fromString(userId), OffsetDateTime.now(), Pageable.ofSize(20));
        VocabCard nextCard = null;
        UserVocabProgress progress = null;
        for (UserVocabProgress p : dueProgress) {
            if (p.getCard().getVocabSet().getId().equals(setId)) {
                progress = p;
                nextCard = p.getCard();
                break;
            }
        }
        if (nextCard == null) {
            // pick first card from set that hasn't been reviewed
            List<VocabCard> cards = vocabCardRepository.findByVocabSetId(UUID.fromString(setId));
            for (VocabCard card : cards) {
                var progOpt = progressRepository.findByUserIdAndCardId(UUID.fromString(userId), card.getId());
                if (progOpt.isEmpty() || progOpt.get().getNextReviewAt().isBefore(OffsetDateTime.now())) {
                    nextCard = card;
                    progress = progOpt.orElse(null);
                    break;
                }
            }
        }
        if (nextCard == null) {
            throw new BusinessRuleException("No cards to review in this set today!");
        }
        long reviewedCount = progressRepository.countByUserIdAndSrsLevelGreaterThanEqual(UUID.fromString(userId), 1);
        long dueCount = progressRepository.countByUserIdAndNextReviewAtBefore(UUID.fromString(userId), OffsetDateTime.now());
        return FlashcardSessionResponse.builder()
                .cardId(nextCard.getId().toString())
                .word(nextCard.getWord())
                .meaning(nextCard.getMeaning())
                .exampleSentence(nextCard.getExampleSentence())
                .phonetic(nextCard.getPhonetic())
                .audioUrl(nextCard.getAudioUrl())
                .imageUrl(nextCard.getImageUrl())
                .currentSrsLevel(progress != null ? progress.getSrsLevel() : 0)
                .totalCardsInSet(set.getCards().size())
                .reviewedCount((int) reviewedCount)
                .cardsDueToday((int) dueCount)
                .build();
    }

    @Override
    @Transactional
    public void submitAnswer(String userId, FlashcardAnswerRequest request) {
        // Chuyển đổi String → UUID
        UUID userUuid = UUID.fromString(userId);
        UUID cardUuid = UUID.fromString(request.getCardId());

        VocabCard card = vocabCardRepository.findById(cardUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        // Lấy hoặc tạo mới UserVocabProgress
        UserVocabProgress progress = progressRepository.findByUserIdAndCardId(userUuid, cardUuid)
                .orElseGet(() -> {
                    // Tạo proxy User (không load toàn bộ)
                    User userProxy = entityManager.getReference(User.class, userUuid);
                    return UserVocabProgress.builder()
                            .id(new UserVocabProgressId(userUuid, cardUuid))
                            .user(userProxy)
                            .card(card)
                            .srsLevel((short) 0)
                            .timesReviewed(0)
                            .streakCorrect((short) 0)
                            .easeFactor(2.5)
                            .nextReviewAt(OffsetDateTime.now())
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                });

        int quality = request.getQuality();
        var srsResult = SrsPolicy.calculate(quality, 0, progress.getTimesReviewed(), progress.getEaseFactor());

        // Cập nhật tiến độ
        progress.setTimesReviewed(srsResult.repetitions());
        progress.setSrsLevel((short) srsResult.repetitions());
        progress.setNextReviewAt(srsResult.nextReview());
        progress.setEaseFactor(srsResult.easeFactor());
        progress.setLastQuality((short) quality);
        progress.setLastReviewedAt(OffsetDateTime.now());
        progress.setUpdatedAt(OffsetDateTime.now());

        if (quality >= 3) {
            progress.setStreakCorrect((short) (progress.getStreakCorrect() + 1));
            // Cộng XP
            int xp = XpPolicy.getXpForQuality(quality);
            profileRepository.addXpAndReview(userUuid, xp);
            if (progress.getTimesReviewed() == 1) {
                profileRepository.incrementTotalWordsLearned(userUuid);
            }
        } else {
            progress.setStreakCorrect((short) 0);
        }

        progressRepository.save(progress);
    }
    // ========== Helpers ==========
    private VocabSet findSetByIdAndUser(String setId, String userId) {
        VocabSet set = vocabSetRepository.findById(UUID.fromString(setId))
                .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
        if (!set.getCreatedBy().getId().equals(userId)) {
            throw new BusinessRuleException("You don't have permission to modify this set");
        }
        return set;
    }

    private VocabCard findCardByIdAndOwnership(String cardId, String userId) {
        VocabCard card = vocabCardRepository.findById(UUID.fromString(cardId))
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        if (!card.getVocabSet().getCreatedBy().getId().equals(userId)) {
            throw new BusinessRuleException("You don't have permission to modify this card");
        }
        return card;
    }
}
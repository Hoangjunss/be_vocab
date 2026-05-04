package com.toiec.demo.service;

import com.toiec.demo.dtos.request.CreateVocabSetRequest;
import com.toiec.demo.dtos.request.CreateVocabCardRequest;
import com.toiec.demo.dtos.request.FlashcardAnswerRequest;
import com.toiec.demo.dtos.response.VocabSetResponse;
import com.toiec.demo.dtos.response.VocabCardResponse;
import com.toiec.demo.entities.User;
import com.toiec.demo.entities.VocabCard;
import com.toiec.demo.entities.VocabSet;
import com.toiec.demo.entities.UserVocabProgress;
import com.toiec.demo.exception.BusinessRuleException;
import com.toiec.demo.mapper.VocabCardMapper;
import com.toiec.demo.mapper.VocabSetMapper;
import com.toiec.demo.repository.GroupRepository;
import com.toiec.demo.repository.UserProfileRepository;
import com.toiec.demo.repository.UserVocabProgressRepository;
import com.toiec.demo.repository.VocabCardRepository;
import com.toiec.demo.repository.VocabSetRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VocabularyServiceImplTest {

    @Mock
    private VocabSetRepository vocabSetRepository;
    @Mock
    private VocabCardRepository vocabCardRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserVocabProgressRepository progressRepository;
    @Mock
    private UserProfileRepository profileRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private VocabSetMapper vocabSetMapper;
    @Mock
    private VocabCardMapper vocabCardMapper;


    @InjectMocks
    private VocabularyServiceImpl vocabularyService;

    private User mockUser;
    private VocabSet mockSet;
    private UUID userId;
    private UUID setId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        setId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(userId);

        mockSet = new VocabSet();
        mockSet.setId(setId);
        mockSet.setTitle("Test Set");
        mockSet.setCreatedBy(mockUser);
        mockSet.setCards(new ArrayList<>());
        
        org.springframework.test.util.ReflectionTestUtils.setField(vocabularyService, "entityManager", entityManager);
    }

    @Test
    void createSet_ShouldSaveAndReturnResponse() {
        CreateVocabSetRequest request = new CreateVocabSetRequest();
        request.setTitle("Test Set");

        VocabSetResponse expectedResponse = VocabSetResponse.builder().title("Test Set").build();

        when(entityManager.getReference(User.class, userId.toString())).thenReturn(mockUser);
        when(vocabSetMapper.toEntity(request)).thenReturn(mockSet);
        when(vocabSetRepository.save(any(VocabSet.class))).thenReturn(mockSet);
        when(vocabSetMapper.toResponse(mockSet)).thenReturn(expectedResponse);

        VocabSetResponse response = vocabularyService.createSet(request, userId.toString());

        assertThat(response.getTitle()).isEqualTo("Test Set");
        verify(vocabSetRepository).save(mockSet);
    }

    @Test
    void createSet_WhenGroupIdNotFound_ShouldThrowException() {
        CreateVocabSetRequest request = new CreateVocabSetRequest();
        request.setTitle("Test Set");
        request.setGroupId(UUID.randomUUID().toString());

        when(entityManager.getReference(User.class, userId.toString())).thenReturn(mockUser);
        when(vocabSetMapper.toEntity(request)).thenReturn(mockSet);
        when(groupRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vocabularyService.createSet(request, userId.toString()))
                .isInstanceOf(com.toiec.demo.exception.ResourceNotFoundException.class)
                .hasMessage("Group not found");
    }

    //  Lỗi so sánh UUID
    @Test
    void updateSet_WhenOwner_ShouldUpdate() {
        com.toiec.demo.dtos.request.UpdateVocabSetRequest request = new com.toiec.demo.dtos.request.UpdateVocabSetRequest();
        request.setTitle("Updated Title");

        VocabSetResponse expectedResponse = VocabSetResponse.builder().title("Updated Title").build();

        when(vocabSetRepository.findById(setId)).thenReturn(Optional.of(mockSet));
        when(vocabSetRepository.save(mockSet)).thenReturn(mockSet);
        when(vocabSetMapper.toResponse(mockSet)).thenReturn(expectedResponse);

        VocabSetResponse response = vocabularyService.updateSet(setId.toString(), request, userId.toString());

        assertThat(response.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void updateSet_WhenNotOwner_ShouldThrowException() {
        com.toiec.demo.dtos.request.UpdateVocabSetRequest request = new com.toiec.demo.dtos.request.UpdateVocabSetRequest();
        String differentUserId = UUID.randomUUID().toString();

        when(vocabSetRepository.findById(setId)).thenReturn(Optional.of(mockSet));

        assertThatThrownBy(() -> vocabularyService.updateSet(setId.toString(), request, differentUserId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("You don't have permission to modify this set");
    }

    //  Lỗi so sánh UUID
    @Test
    void deleteSet_WhenOwner_ShouldDelete() {
        when(vocabSetRepository.findById(setId)).thenReturn(Optional.of(mockSet));

        vocabularyService.deleteSet(setId.toString(), userId.toString());

        verify(vocabSetRepository).delete(mockSet);
    }

    //  Lỗi so sánh UUID
    @Test
    void addCard_WhenCreator_ShouldAddCard() {
        CreateVocabCardRequest request = new CreateVocabCardRequest();
        request.setWord("abandon");

        VocabCard mockCard = new VocabCard();
        mockCard.setWord("abandon");
        
        VocabCardResponse expectedResponse = VocabCardResponse.builder().word("abandon").build();

        when(vocabSetRepository.findById(setId)).thenReturn(Optional.of(mockSet));
        when(vocabCardMapper.toEntity(request)).thenReturn(mockCard);
        when(vocabCardRepository.save(any(VocabCard.class))).thenReturn(mockCard);
        when(vocabCardMapper.toResponse(mockCard)).thenReturn(expectedResponse);

        VocabCardResponse response = vocabularyService.addCard(setId.toString(), request, userId.toString());

        assertThat(response.getWord()).isEqualTo("abandon");
    }

    // Lỗi so sánh kieuer dữ liệu VocabularyServiceImpl:283
    @Test
    void addCard_WhenNotCreator_ShouldThrowException() {
        CreateVocabCardRequest request = new CreateVocabCardRequest();
        String differentUserId = UUID.randomUUID().toString();

        when(vocabSetRepository.findById(setId)).thenReturn(Optional.of(mockSet));

        assertThatThrownBy(() -> vocabularyService.addCard(setId.toString(), request, differentUserId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("You don't have permission to modify this set");
    }

    @Test
    void submitAnswer_WithCorrectQuality_ShouldUpdateSrsAndAddXp() {
        UUID cardId = UUID.randomUUID();
        FlashcardAnswerRequest request = new FlashcardAnswerRequest();
        request.setCardId(cardId.toString());
        request.setQuality(4);

        VocabCard card = new VocabCard();
        card.setId(cardId);
        
        UserVocabProgress progress = new UserVocabProgress();
        progress.setTimesReviewed(0);
        progress.setEaseFactor(2.5);
        progress.setStreakCorrect((short) 0);

        when(vocabCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(progressRepository.findByUserIdAndCardId(userId, cardId)).thenReturn(Optional.of(progress));

        vocabularyService.submitAnswer(userId.toString(), request);

        verify(progressRepository).save(progress);
        verify(profileRepository).addXpAndReview(eq(userId), eq(15));
        assertThat(progress.getStreakCorrect()).isEqualTo((short) 1);
    }

    @Test
    void submitAnswer_WithIncorrectQuality_ShouldResetStreak() {
        UUID cardId = UUID.randomUUID();
        FlashcardAnswerRequest request = new FlashcardAnswerRequest();
        request.setCardId(cardId.toString());
        request.setQuality(2); // incorrect (<3)

        VocabCard card = new VocabCard();
        card.setId(cardId);
        
        UserVocabProgress progress = new UserVocabProgress();
        progress.setTimesReviewed(5);
        progress.setEaseFactor(2.5);
        progress.setStreakCorrect((short) 5);

        when(vocabCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(progressRepository.findByUserIdAndCardId(userId, cardId)).thenReturn(Optional.of(progress));

        vocabularyService.submitAnswer(userId.toString(), request);

        verify(progressRepository).save(progress);
        verify(profileRepository, never()).addXpAndReview(any(), anyInt());
        assertThat(progress.getStreakCorrect()).isEqualTo((short) 0);
        assertThat(progress.getTimesReviewed()).isEqualTo(0);
    }

    // Lỗi so sánh UUID bên serivce
    @Test
    void getNextFlashcard_ShouldReturnDueCard() {
        VocabCard card = new VocabCard();
        card.setId(UUID.randomUUID());
        card.setWord("due-word");
        card.setVocabSet(mockSet);
        
        UserVocabProgress progress = new UserVocabProgress();
        progress.setCard(card);
        progress.setSrsLevel((short) 1);

        when(vocabSetRepository.findById(setId)).thenReturn(Optional.of(mockSet));
        when(progressRepository.findByUserIdAndNextReviewAtBefore(eq(userId), any(), any()))
                .thenReturn(java.util.Collections.singletonList(progress));

        com.toiec.demo.dtos.response.FlashcardSessionResponse response = vocabularyService.getNextFlashcard(userId.toString(), setId.toString());

        assertThat(response.getWord()).isEqualTo("due-word");
    }

    //  Lỗi so sánh UUID
    @Test
    void importCardsFromFile_WhenInvalidFile_ShouldThrowException() {
        MultipartFile file = mock(MultipartFile.class);
        when(vocabSetRepository.findById(setId)).thenReturn(Optional.of(mockSet));
        
        try {
            when(file.getInputStream()).thenThrow(new RuntimeException("IO Error"));
        } catch (Exception e) {}

        assertThatThrownBy(() -> vocabularyService.importCardsFromFile(setId.toString(), file, userId.toString()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Failed to parse file");
    }
}

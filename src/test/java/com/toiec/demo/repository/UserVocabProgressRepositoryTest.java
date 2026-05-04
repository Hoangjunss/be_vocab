package com.toiec.demo.repository;

import com.toiec.demo.entities.User;
import com.toiec.demo.entities.UserVocabProgress;
import com.toiec.demo.entities.UserVocabProgressId;
import com.toiec.demo.entities.VocabCard;
import com.toiec.demo.entities.VocabSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserVocabProgressRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserVocabProgressRepository progressRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedUser;
    private VocabCard savedCard;
    private UserVocabProgress savedProgress;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("progresstest@example.com");
        user.setPasswordHash("hash");
        user.setFullName("Progress Tester");
        user.setRole("USER");
        savedUser = entityManager.persistAndFlush(user);

        VocabSet set = new VocabSet();
        set.setTitle("Progress Set");
        set.setPublic(true);
        set.setCreatedBy(savedUser);
        VocabSet savedSet = entityManager.persistAndFlush(set);

        VocabCard card = new VocabCard();
        card.setWord("test");
        card.setMeaning("test meaning");
        card.setVocabSet(savedSet);
        savedCard = entityManager.persistAndFlush(card);

        UserVocabProgress progress = new UserVocabProgress();
        progress.setId(new UserVocabProgressId(savedUser.getId(), savedCard.getId()));
        progress.setUser(savedUser);
        progress.setCard(savedCard);
        progress.setSrsLevel((short) 1);
        progress.setTimesReviewed(1);
        progress.setStreakCorrect((short) 1);
        progress.setEaseFactor(2.5);
        progress.setNextReviewAt(OffsetDateTime.now().minusDays(1)); // Due
        progress.setCreatedAt(OffsetDateTime.now());
        progress.setUpdatedAt(OffsetDateTime.now());
        savedProgress = entityManager.persistAndFlush(progress);
    }

    @Test
    void findByUserIdAndCardId_ShouldReturnProgress() {
        Optional<UserVocabProgress> result = progressRepository.findByUserIdAndCardId(savedUser.getId(), savedCard.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getSrsLevel()).isEqualTo((short) 1);
    }

    @Test
    void countByUserIdAndNextReviewAtBefore_ShouldCountDueCards() {
        long count = progressRepository.countByUserIdAndNextReviewAtBefore(savedUser.getId(), OffsetDateTime.now());
        
        assertThat(count).isEqualTo(1);
    }

    @Test
    void incrementSrsLevel_ShouldUpdateAtomically() {
        progressRepository.incrementSrsLevel(savedUser.getId(), savedCard.getId());
        entityManager.clear();

        UserVocabProgress updatedProgress = progressRepository.findById(savedProgress.getId()).orElseThrow();
        assertThat(updatedProgress.getSrsLevel()).isEqualTo((short) 2);
    }

    @Test
    void findByUserIdAndCardId_WhenNotExists_ShouldReturnEmpty() {
        Optional<UserVocabProgress> result = progressRepository.findByUserIdAndCardId(
                savedUser.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndNextReviewAtBefore_ShouldRespectPagination() {
        VocabCard card2 = new VocabCard();
        card2.setWord("test 2");
        card2.setMeaning("meaning 2");
        card2.setVocabSet(savedCard.getVocabSet());
        VocabCard savedCard2 = entityManager.persistAndFlush(card2);

        UserVocabProgress prog2 = new UserVocabProgress();
        prog2.setId(new UserVocabProgressId(savedUser.getId(), savedCard2.getId()));
        prog2.setUser(savedUser);
        prog2.setCard(savedCard2);
        prog2.setNextReviewAt(OffsetDateTime.now().minusHours(1));
        entityManager.persistAndFlush(prog2);

        List<UserVocabProgress> results = progressRepository.findByUserIdAndNextReviewAtBefore(
                savedUser.getId(), OffsetDateTime.now(), PageRequest.of(0, 1));

        assertThat(results).hasSize(1);
    }

    @Test
    void countByUserIdAndSrsLevelGreaterThanEqual_ShouldReturnCorrectCount() {
        long countAtLevel1 = progressRepository.countByUserIdAndSrsLevelGreaterThanEqual(savedUser.getId(), 1);
        long countAtLevel5 = progressRepository.countByUserIdAndSrsLevelGreaterThanEqual(savedUser.getId(), 5);

        assertThat(countAtLevel1).isEqualTo(1);
        assertThat(countAtLevel5).isEqualTo(0);
    }

    @Test
    void countByUserIdAndNextReviewAtBefore_FutureDate_ShouldReturnZero() {
        savedProgress.setNextReviewAt(OffsetDateTime.now().plusDays(1));
        entityManager.persistAndFlush(savedProgress);

        long countNow = progressRepository.countByUserIdAndNextReviewAtBefore(savedUser.getId(), OffsetDateTime.now());

        assertThat(countNow).isEqualTo(0);
    }

}

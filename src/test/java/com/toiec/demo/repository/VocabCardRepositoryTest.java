package com.toiec.demo.repository;

import com.toiec.demo.entities.User;
import com.toiec.demo.entities.VocabCard;
import com.toiec.demo.entities.VocabSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VocabCardRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private VocabCardRepository vocabCardRepository;

    @Autowired
    private TestEntityManager entityManager;

    private VocabSet savedSet;
    private VocabCard savedCard1;
    private VocabCard savedCard2;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("cardcreator@example.com");
        user.setPasswordHash("hash");
        user.setFullName("Card Creator");
        user.setRole("USER");
        User savedUser = entityManager.persistAndFlush(user);

        VocabSet set = new VocabSet();
        set.setTitle("Test Set");
        set.setPublic(true);
        set.setCreatedBy(savedUser);
        savedSet = entityManager.persistAndFlush(set);

        VocabCard card1 = new VocabCard();
        card1.setWord("abandon");
        card1.setMeaning("leave");
        card1.setVocabSet(savedSet);
        savedCard1 = entityManager.persistAndFlush(card1);

        VocabCard card2 = new VocabCard();
        card2.setWord("ability");
        card2.setMeaning("power");
        card2.setVocabSet(savedSet);
        savedCard2 = entityManager.persistAndFlush(card2);
    }

    @Test
    void findByVocabSetId_ShouldReturnCards() {
        List<VocabCard> cards = vocabCardRepository.findByVocabSetId(savedSet.getId());

        assertThat(cards).hasSize(2);
        assertThat(cards).extracting(VocabCard::getWord).containsExactlyInAnyOrder("abandon", "ability");
    }

    @Test
    void findByIdAndVocabSetId_ShouldReturnCard() {
        Optional<VocabCard> result = vocabCardRepository.findByIdAndVocabSetId(savedCard1.getId(), savedSet.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getWord()).isEqualTo("abandon");
    }

    @Test
    void deleteByIdAndVocabSetId_ShouldDeleteCard() {
        vocabCardRepository.deleteByIdAndVocabSetId(savedCard1.getId(), savedSet.getId());
        entityManager.flush();
        entityManager.clear();

        Optional<VocabCard> result = vocabCardRepository.findById(savedCard1.getId());
        assertThat(result).isEmpty();

        List<VocabCard> remaining = vocabCardRepository.findByVocabSetId(savedSet.getId());
        assertThat(remaining).hasSize(1);
    }

    @Test
    void deleteAllByVocabSetId_ShouldDeleteAllCardsInSet() {
        vocabCardRepository.deleteAllByVocabSetId(savedSet.getId());
        entityManager.flush();
        entityManager.clear();

        List<VocabCard> remaining = vocabCardRepository.findByVocabSetId(savedSet.getId());
        assertThat(remaining).isEmpty();
    }
}

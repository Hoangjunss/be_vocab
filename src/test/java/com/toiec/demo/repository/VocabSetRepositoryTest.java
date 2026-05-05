package com.toiec.demo.repository;

import com.toiec.demo.entities.Group;
import com.toiec.demo.entities.User;
import com.toiec.demo.entities.VocabSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VocabSetRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private VocabSetRepository vocabSetRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedUser;
    private Group savedGroup;
    private VocabSet publicSet;
    private VocabSet privateSet;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("setcreator@example.com");
        user.setPasswordHash("hash");
        user.setFullName("Set Creator");
        user.setRole("USER");
        savedUser = entityManager.persistAndFlush(user);

        Group group = new Group();
        group.setName("English Basics Group");
        group.setPublic(true);
        group.setCreatedBy(savedUser.getId());
        savedGroup = entityManager.persistAndFlush(group);

        VocabSet set1 = new VocabSet();
        set1.setTitle("Public Basics Set");
        set1.setPublic(true);
        set1.setCreatedBy(savedUser);
        set1.setGroup(savedGroup);
        publicSet = entityManager.persistAndFlush(set1);

        VocabSet set2 = new VocabSet();
        set2.setTitle("Private Basics Set");
        set2.setPublic(false);
        set2.setCreatedBy(savedUser);
        set2.setGroup(savedGroup);
        privateSet = entityManager.persistAndFlush(set2);
    }

    @Test
    void findByIsPublicTrue_ShouldReturnOnlyPublicSets() {
        Page<VocabSet> result = vocabSetRepository.findByIsPublicTrue(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Public Basics Set");
    }

    @Test
    void findByIsPublicTrueAndGroup_NameContainingIgnoreCase_ShouldReturnMatchingSets() {
        Page<VocabSet> result = vocabSetRepository.findByIsPublicTrueAndGroup_NameContainingIgnoreCase("english", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Public Basics Set");

        Page<VocabSet> emptyResult = vocabSetRepository.findByIsPublicTrueAndGroup_NameContainingIgnoreCase("french", PageRequest.of(0, 10));
        assertThat(emptyResult.getContent()).isEmpty();
    }

    @Test
    void findByCreatedById_ShouldReturnUserSets() {
        Page<VocabSet> result = vocabSetRepository.findByCreatedById(savedUser.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByIdAndCreatedById_ShouldReturnSet() {
        Optional<VocabSet> result = vocabSetRepository.findByIdAndCreatedById(publicSet.getId(), savedUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Public Basics Set");
    }

    @Test
    void countByCreatedById_ShouldReturnSetCount() {
        long count = vocabSetRepository.countByCreatedById(savedUser.getId());

        assertThat(count).isEqualTo(2);
    }
}

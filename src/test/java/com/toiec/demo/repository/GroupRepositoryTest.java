package com.toiec.demo.repository;

import com.toiec.demo.entities.Group;
import com.toiec.demo.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GroupRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedUser;
    private Group publicGroup;
    private Group privateGroup;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("groupcreator@example.com");
        user.setPasswordHash("hash");
        user.setFullName("Group Creator");
        user.setRole("USER");
        savedUser = entityManager.persistAndFlush(user);

        Group group1 = new Group();
        group1.setName("Public TOEIC");
        group1.setPublic(true);
        group1.setCreatedBy(savedUser.getId());
        publicGroup = entityManager.persistAndFlush(group1);

        Group group2 = new Group();
        group2.setName("Private TOEIC");
        group2.setPublic(false);
        group2.setCreatedBy(savedUser.getId());
        privateGroup = entityManager.persistAndFlush(group2);
    }

    @Test
    void findByIsPublicTrue_ShouldReturnOnlyPublicGroups() {
        // Act
        Page<Group> result = groupRepository.findByIsPublicTrue(PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Public TOEIC");
    }

    @Test
    void findByCreatedBy_ShouldReturnAllUserGroups() {
        // Act
        Page<Group> result = groupRepository.findByCreatedBy(savedUser.getId(), PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findByIdAndCreatedBy_WhenMatching_ShouldReturnGroup() {
        // Act
        Optional<Group> result = groupRepository.findByIdAndCreatedBy(publicGroup.getId(), savedUser.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Public TOEIC");
    }

    @Test
    void findByIdAndCreatedBy_WhenNotMatchingUser_ShouldReturnEmpty() {
        // Act
        // Use a different user ID
        Optional<Group> result = groupRepository.findByIdAndCreatedBy(publicGroup.getId(), java.util.UUID.randomUUID());

        // Assert
        assertThat(result).isEmpty();
    }
}

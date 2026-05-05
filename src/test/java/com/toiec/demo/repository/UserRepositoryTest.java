package com.toiec.demo.repository;

import com.toiec.demo.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setFullName("Test User");
        user.setRole("USER");
        user.setActive(true);
        savedUser = entityManager.persistAndFlush(user);
    }

    @Test
    void findByEmail_WhenEmailExists_ShouldReturnUser() {
        // Act
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Test User");
    }

    @Test
    void findByEmail_WhenEmailDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<User> result = userRepository.findByEmail("notfound@example.com");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void softDelete_ShouldSetDeletedAtTimestamp() {
        // Act
        userRepository.softDelete(savedUser.getId());
        entityManager.clear(); // Clear L1 cache to fetch from DB

        // Assert
        User deletedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(deletedUser.getDeletedAt()).isNotNull();
    }
}

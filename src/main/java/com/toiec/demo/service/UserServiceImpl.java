package com.toiec.demo.service;

import com.toiec.demo.dtos.request.LoginRequest;
import com.toiec.demo.dtos.request.RegisterRequest;
import com.toiec.demo.dtos.response.AuthResponse;
import com.toiec.demo.dtos.response.UserProfileResponse;
import com.toiec.demo.entities.User;
import com.toiec.demo.entities.UserProfile;
import com.toiec.demo.exception.BusinessRuleException;
import com.toiec.demo.mapper.UserMapper;
import com.toiec.demo.repository.UserProfileRepository;
import com.toiec.demo.repository.UserRepository;
import com.toiec.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import java.util.Collections;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    @Value("${google.client.id}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        log.info("=== START loginWithGoogle ===");
        log.info("idToken length: {}", idToken != null ? idToken.length() : 0);
        if (idToken == null) log.warn("idToken is null!");
        try {
            log.info("Creating GoogleIdTokenVerifier...");
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new JacksonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            log.info("Verifier created, googleClientId: {}", googleClientId);

            GoogleIdToken googleToken = verifier.verify(idToken);
            log.info("googleToken after verify: {}", googleToken);
            if (googleToken == null) {
                log.error("googleToken is null, verification failed");
                throw new BusinessRuleException("Invalid Google token");
            }
            GoogleIdToken.Payload payload = googleToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String avatarUrl = (String) payload.get("picture");
            log.info("payload - email: {}, name: {}, avatarUrl: {}", email, name, avatarUrl);

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                log.info("User not found, creating new...");
                User newUser = User.builder()
                        .email(email)
                        .fullName(name)
                        .avatarUrl(avatarUrl)
                        .passwordHash("")
                        .role("USER")
                        .isActive(true)
                        .build();
                log.info("newUser before save, id: {}", newUser.getId());
                User savedUser = userRepository.save(newUser);
                log.info("savedUser after save, id: {}", savedUser.getId());
                if (savedUser.getId() == null) log.error("savedUser.getId() is null!");
                UserProfile profile = UserProfile.builder()
                        .user(savedUser)
                        .build();
                log.info("profile created, userId: {}", profile.getUserId());
                UserProfile savedProfile = profileRepository.save(profile);
                log.info("profile saved, userId: {}", savedProfile.getUserId());
                return savedUser;
            });
            log.info("User found/created, id: {}", user.getId());

            String accessToken = tokenProvider.generateAccessToken(user);
            String refreshToken = tokenProvider.generateRefreshToken(user);
            log.info("Tokens generated");
            return new AuthResponse(accessToken, refreshToken, "Bearer");
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new BusinessRuleException("Google authentication failed: " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already in use");
        }
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .userId(UUID.fromString(user.getId().toString()))
                .user(user)
                .build();
        profileRepository.save(profile);

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessRuleException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Invalid credentials");
        }
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }

    @Override
    public UserProfileResponse getProfile(String userId) {
        UserProfile profile = profileRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessRuleException("Profile not found"));
        return userMapper.toProfileResponse(profile);
    }

    @Override
    public void logout(String token) {
        // Invalidate token (implement token blacklist in Redis if needed)
        log.info("Logout token: {}", token);
    }
}
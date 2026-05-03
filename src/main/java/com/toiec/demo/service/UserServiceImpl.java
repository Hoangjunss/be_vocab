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
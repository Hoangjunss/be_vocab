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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository profileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@example.com");
        mockUser.setPasswordHash("encoded_password");
    }

    @Test
    void register_WhenEmailNotExists_ShouldRegisterAndReturnTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(mockUser);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(profileRepository.save(any(UserProfile.class))).thenReturn(new UserProfile());
        when(tokenProvider.generateAccessToken(mockUser)).thenReturn("access_token");
        when(tokenProvider.generateRefreshToken(mockUser)).thenReturn("refresh_token");

        AuthResponse response = userService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access_token");
        verify(userRepository).save(mockUser);
        verify(profileRepository).save(any(UserProfile.class));
    }

    @Test
    void register_WhenEmailExists_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
        when(tokenProvider.generateAccessToken(mockUser)).thenReturn("access_token");

        AuthResponse response = userService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access_token");
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");

        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void getProfile_WhenProfileExists_ShouldReturnResponse() {
        UserProfile profile = new UserProfile();
        profile.setUser(mockUser);
        
        UserProfileResponse expectedResponse = UserProfileResponse.builder().email("test@example.com").build();

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(userMapper.toProfileResponse(profile)).thenReturn(expectedResponse);

        UserProfileResponse response = userService.getProfile(userId.toString());

        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getProfile_WhenNotExists_ShouldThrowException() {
        when(profileRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(UUID.randomUUID().toString()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Profile not found");
    }
}

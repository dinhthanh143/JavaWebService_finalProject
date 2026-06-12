package com.example.demo.service;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_old_password")
                .email("test@gmail.com")
                .role("ROLE_USER")
                .build();
    }

    // --- TEST CASE 1: ĐĂNG KÝ THÀNH CÔNG ---
    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("newuser", "password", "Full Name", "new@gmail.com", "090");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");

        String result = authService.register(request);

        assertEquals("Đăng ký thành công", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // --- TEST CASE 2: ĐĂNG KÝ THẤT BẠI VÌ TRÙNG USERNAME ---
    @Test
    void register_ThrowsException_WhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("testuser", "password", "Full Name", "new@gmail.com", "090");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    // --- TEST CASE 3: ĐỔI MẬT KHẨU THÀNH CÔNG ---
    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("old_pass", "new_pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("old_pass", mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new_pass")).thenReturn("encoded_new_password");

        String result = authService.changePassword(1L, request);

        assertEquals("Đổi mật khẩu thành công!", result);
        assertEquals("encoded_new_password", mockUser.getPassword());
    }

    // --- TEST CASE 4: ĐỔI MẬT KHẨU THẤT BẠI VÌ SAI MẬT KHẨU CŨ ---
    @Test
    void changePassword_ThrowsException_WhenOldPasswordIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrong_old_pass", "new_pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong_old_pass", mockUser.getPassword())).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.changePassword(1L, request));

        assertEquals("Mật khẩu cũ không chính xác!", exception.getMessage());
    }

    // --- TEST CASE 5: ĐỔI MẬT KHẨU THẤT BẠI VÌ TRÙNG MẬT KHẨU MỚI ---
    @Test
    void changePassword_ThrowsException_WhenNewPasswordEqualsOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("same_pass", "same_pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("same_pass", mockUser.getPassword())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.changePassword(1L, request));

        assertEquals("Mật khẩu mới không được trùng với mật khẩu cũ!", exception.getMessage());
    }
}
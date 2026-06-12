package com.example.demo.service;


import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.exception.BadRequestException;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.PasswordResetOtp;
import com.example.demo.model.Token;
import com.example.demo.model.TokenBlacklist;
import com.example.demo.model.User;
import com.example.demo.repository.PasswordResetOtpRepository;
import com.example.demo.repository.TokenBlacklistRepository;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtProvider;
import com.example.demo.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private PasswordResetOtpRepository otpRepository;


    private void saveUserToken(UserPrincipal principal , String jwt){
        User u = principal.getUser();
        Token t = Token.builder()
                .user(u)
                .tokenValue(jwt)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(t);
    }

    private void revokeAllToken(UserPrincipal principal){
        User u = principal.getUser();
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(u.getId());
        if(validUserTokens.isEmpty()) return;
        validUserTokens.forEach(token ->{
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserPrincipal userPrincipal = userRepository.findByUsername(request.getUsername())
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + request.getUsername()));

        String accessToken = jwtProvider.generateAccessToken(userPrincipal);
        String refreshToken = jwtProvider.generateRefreshToken(userPrincipal);
        revokeAllToken(userPrincipal);
        saveUserToken(userPrincipal, refreshToken);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(userPrincipal.getUsername())
                .role(userPrincipal.getUser().getRole())
                .build();
    }

    public String register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên tài khoản đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User u = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role("ROLE_USER")
                .isEnabled(true)
                .build();

        userRepository.save(u);
        return "Đăng ký thành công";
    }


    @Transactional
    public AuthResponse refreshToken(String token, String oldToken) {
        Token tokenReal = tokenRepository.findByTokenValue(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        if (tokenReal.isRevoked() || tokenReal.isExpired()) {
            throw new RuntimeException("Refresh token đã bị vô hiệu hóa hoặc hết hạn");
        }

        String tokenValue = tokenReal.getTokenValue();
        String username = jwtProvider.getUserNameFromToken(tokenValue);

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);

        if (jwtProvider.isTokenExpired(tokenValue) || !jwtProvider.isTokenValid(tokenValue, userPrincipal)) {
            throw new RuntimeException("Token hết hạn hoặc không hợp lệ về mặt chữ ký");
        }
        if (oldToken != null && oldToken.startsWith("Bearer ")) {
            String cleanRawToken = oldToken.substring(7);

            if (!tokenBlacklistRepository.existsByToken(cleanRawToken)) {
                TokenBlacklist blacklist = TokenBlacklist.builder()
                        .token(cleanRawToken)
                        .expiryTime(java.time.LocalDateTime.now().plusMinutes(15))
                        .build();
                tokenBlacklistRepository.save(blacklist);
            }
        }

        String accessToken = jwtProvider.generateAccessToken(userPrincipal);
        String refreshToken = jwtProvider.generateRefreshToken(userPrincipal);

        tokenReal.setRevoked(true);
        tokenReal.setExpired(true);
        tokenRepository.save(tokenReal);

        saveUserRefreshToken(userPrincipal, refreshToken);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(userPrincipal.getUsername())
                .role(userPrincipal.getUser().getRole())
                .build();
    }




    private void saveUserRefreshToken(UserPrincipal userPrincipal, String refreshToken) {
        User user = userPrincipal.getUser();
        Token token = Token.builder()
                .user(user)
                .tokenValue(refreshToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    @Transactional
    public String changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác!");
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ!");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        return "Đổi mật khẩu thành công!";
    }

    @Transactional
    public String requestForgotPasswordOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email không được để trống!");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nào đăng ký với email này!"));

        otpRepository.deleteByEmail(email.trim());

        String generatedOtp = String.valueOf((int) ((Math.random() * (999999 - 100000)) + 100000));

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .email(email.trim())
                .otp(generatedOtp)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        otpRepository.save(passwordResetOtp);

        System.out.println("=== SYSTEM OTP CHO EMAIL [" + email + "] LÀ: " + generatedOtp + " ===");

        return "Mã OTP đặt lại mật khẩu đã được tạo thành công! (Vui lòng kiểm tra log hệ thống)";
    }

    @Transactional
    public String resetPasswordWithOtp(String email, String otp, String newPassword) {
        if (otp == null || otp.trim().isEmpty()) {
            throw new BadRequestException("Mã OTP không được để trống!");
        }
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new BadRequestException("Mật khẩu mới phải có tối thiểu 6 ký tự!");
        }

        PasswordResetOtp resetOtp = otpRepository.findByEmailAndOtp(email.trim(), otp.trim())
                .orElseThrow(() -> new BadRequestException("Mã OTP không chính xác hoặc không hợp lệ!"));

        // 2. Kiểm tra xem mã OTP này đã bị quá 5 phút chưa
        if (resetOtp.isExpired()) {
            otpRepository.delete(resetOtp); // Xóa luôn bản ghi hết hạn
            throw new BadRequestException("Mã OTP này đã hết hạn sử dụng. Vui lòng yêu cầu mã mới!");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản liên kết với email này không còn tồn tại!"));

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        otpRepository.delete(resetOtp);

        return "Thay đổi mật khẩu mới thành công!";
    }



}




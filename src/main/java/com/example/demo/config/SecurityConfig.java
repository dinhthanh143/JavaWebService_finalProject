package com.example.demo.config;

import com.example.demo.model.TokenBlacklist;
import com.example.demo.repository.TokenBlacklistRepository;
import com.example.demo.security.JwtAuthenticationEntryPoint;
import com.example.demo.security.JwtFilter;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.TokenRepository;
import com.example.demo.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/bookings/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                        .requestMatchers("/api/v1/courts/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String authHeader = request.getHeader("Authorization");
                            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                String accessToken = authHeader.substring(7);
                                TokenBlacklist blacklist = TokenBlacklist.builder()
                                        .token(accessToken)
                                        .expiryTime(java.time.LocalDateTime.now().plusMinutes(15))
                                        .build();
                                tokenBlacklistRepository.save(blacklist);
                            }

                            String refreshToken = request.getParameter("refreshToken");
                            if (refreshToken != null) {
                                tokenRepository.findByTokenValue(refreshToken).ifPresent(token -> {
                                    token.setExpired(true);
                                    token.setRevoked(true);
                                    tokenRepository.save(token);
                                });
                            }
                            SecurityContextHolder.clearContext();
                            response.setStatus(200);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"message\": \"Đăng xuất thành công\"}");
                        })
                ).exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );

        return http.build();
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
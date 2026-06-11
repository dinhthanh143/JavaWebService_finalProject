package com.example.demo.service;

import com.example.demo.dto.UserResponse;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Page getAllUsers(int page, Long id, String username, String role) {
        
    }

    public UserResponse toggleUserStatus(Long id, boolean isEnabled){

    }


}

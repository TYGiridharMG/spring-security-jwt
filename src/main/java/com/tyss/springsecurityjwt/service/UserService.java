package com.tyss.springsecurityjwt.service;

import org.springframework.stereotype.Service;

import com.tyss.springsecurityjwt.dto.UserSummary;
import com.tyss.springsecurityjwt.security.UserPrincipal;

@Service
public class UserService {

    public UserSummary getCurrentUser(UserPrincipal userPrincipal) {
        return UserSummary.builder()
                .id(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .name(userPrincipal.getName())
                .build();
    }
}

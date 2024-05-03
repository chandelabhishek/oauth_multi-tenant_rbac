package com.oauth.example.domain.mapper;

import com.oauth.example.domain.entity.User;
import com.oauth.example.domain.model.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private UserPrincipal userPrincipal;

    public UserDetails mapUserToUserDetails(User user) {
        userPrincipal.setUser(user);
        return userPrincipal;
    }
}

package com.oauth.example.service.email;

import com.oauth.example.domain.entity.Tenant;
import com.oauth.example.domain.entity.User;

import java.io.IOException;

public interface EmailService {
    void sendForgotPasswordEmail(String recoveryLink, User user) throws IOException;

    void sendUserInviteEmail(User user, String userInviteLink, Tenant tenant) throws IOException;
}

package com.oauth.example.config;

import com.sendgrid.SendGrid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SendgridConfig {
    @Value("${sendgrid.apiKey}")
    private String sendGridAPIKey;

    @Value("${sendgrid.forgotPasswordTemplateId}")
    private String forgotPasswordTemplateId;

    @Value("${sendgrid.userInviteTemplateId}")
    private String userInviteTemplateId;

    @Value("${sendgrid.sender}")
    private String senderEmail;

    @Value("${sendgrid.senderName}")
    private String senderName;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendGridAPIKey);
    }
}

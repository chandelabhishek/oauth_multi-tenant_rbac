package com.oauth.example.config.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;

@Configuration
@Getter
public class EncryptorConfig {
    @Value("${app.encryptionKey}")
    private String encryptionKey;
    @Value("${app.passkey}")
    private String passkey;

    @Bean
    public BytesEncryptor bytesEncryptor() {
        return Encryptors.stronger(passkey, encryptionKey);
    }
}

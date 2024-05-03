package com.oauth.example.util;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomStringGenerator {

    @Autowired
    private SecureRandom secureRandom;

    private String generateRandomSecureString() {
        byte[] values = new byte[32];
        secureRandom.setSeed(secureRandom.generateSeed(5));
        secureRandom.nextBytes(values);
        return Base64.toBase64String(values)
                .replace("=", "");
    }

    public String clientIdGenerator() {
        secureRandom.setSeed(secureRandom.generateSeed(10));
        return String.valueOf(secureRandom.nextLong(Long.MAX_VALUE - 9999999999L + 1) + 9999999999L);
    }

    public String clientSecretGenerator() {
        return generateRandomSecureString();
    }
}

package com.oauth.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Service
public class CryptoService {

    @Autowired
    private BytesEncryptor encryptor;

    public String encrypt(String textToEncrypt) {
        return Base64.getEncoder().encodeToString(encryptor.encrypt(textToEncrypt.getBytes()));
    }

    public String decrypt(String encryptedText) {
        byte[] decodedByte = Base64.getDecoder().decode(encryptedText);
        return new String(encryptor.decrypt(decodedByte), StandardCharsets.UTF_8);
    }
}

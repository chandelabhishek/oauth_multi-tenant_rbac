package com.oauth.example.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {CryptoService.class})
@ExtendWith(SpringExtension.class)
class CryptoServiceTest {
    @MockBean
    private BytesEncryptor bytesEncryptor;

    @Autowired
    private CryptoService cryptoService;

    /**
     * Method under test: {@link CryptoService#encrypt(String)}
     */
    @Test
    void testEncrypt() throws UnsupportedEncodingException {
        when(bytesEncryptor.encrypt(Mockito.any())).thenReturn("AXAXAXAX".getBytes(StandardCharsets.UTF_8));
        assertEquals("QVhBWEFYQVg=", cryptoService.encrypt("Text To Encrypt"));
        verify(bytesEncryptor).encrypt(Mockito.any());
    }

    /**
     * Method under test: {@link CryptoService#decrypt(String)}
     */
    @Test
    void test_Decrypt() throws UnsupportedEncodingException {
        when(bytesEncryptor.decrypt(Mockito.any())).thenReturn("AXAXAXAX".getBytes(StandardCharsets.UTF_8));
        assertEquals("AXAXAXAX", cryptoService.decrypt("42"));
        verify(bytesEncryptor).decrypt(Mockito.any());
    }
}


package com.oauth.example.util;

import com.google.common.primitives.Ints;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.SecureRandom;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {RandomStringGenerator.class, SecureRandom.class})
@ExtendWith(SpringExtension.class)
class RandomStringGeneratorTest {
    @Autowired
    private RandomStringGenerator randomStringGenerator;

    @MockBean
    private SecureRandom secureRandom;

    /**
     * Method under test: {@link RandomStringGenerator#clientIdGenerator()}
     */
    @Test
    void testClientIdGenerator() {
        Mockito.when(secureRandom.generateSeed(10)).thenReturn(Ints.toByteArray(Integer.MAX_VALUE));

        // Arrange and Act
        randomStringGenerator.clientIdGenerator();

        //assert
        Mockito.verify(secureRandom).setSeed(Ints.toByteArray(Integer.MAX_VALUE));
        Mockito.verify(secureRandom).generateSeed(10);
        Mockito.verify(secureRandom).nextLong(Long.MAX_VALUE - 9999999999L + 1);
    }

    /**
     * Method under test: {@link RandomStringGenerator#clientSecretGenerator()}
     */
    @Test
    void testClientSecretGenerator() {
        Mockito.when(secureRandom.generateSeed(5)).thenReturn(Ints.toByteArray(Integer.MAX_VALUE));
        Mockito.doNothing().when(secureRandom).nextBytes(Mockito.any(byte[].class));

        // Arrange and Act
        randomStringGenerator.clientSecretGenerator();

        //assert
        Mockito.verify(secureRandom).setSeed(Mockito.any(byte[].class));
        Mockito.verify(secureRandom).generateSeed(5);
        Mockito.verify(secureRandom).nextBytes(Mockito.any(byte[].class));

    }
}

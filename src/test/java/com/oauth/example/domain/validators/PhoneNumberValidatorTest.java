package com.oauth.example.domain.validators;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PhoneNumberValidatorTest {

    /**
     * Method under test: {@link PhoneNumberValidator#isValid(String, ConstraintValidatorContext)}
     */
    @Test
    void test_isValid() throws NumberParseException {
        PhoneNumberUtil phoneNumberUtilMock = Mockito.mock(PhoneNumberUtil.class);
        PhoneNumberValidator phoneNumberValidator = new PhoneNumberValidator(phoneNumberUtilMock);
        doReturn(true).when(phoneNumberUtilMock).isValidNumber(Mockito.any());
        when(phoneNumberUtilMock.parse(Mockito.any(), Mockito.any()))
                .thenReturn(new Phonenumber.PhoneNumber());
        ClockProvider clockProvider = mock(ClockProvider.class);
        assertTrue(phoneNumberValidator.isValid("+917425222599",
                new ConstraintValidatorContextImpl(clockProvider, PathImpl.createRootPath(), null,
                        "Constraint Validator Payload", ExpressionLanguageFeatureLevel.DEFAULT,
                        ExpressionLanguageFeatureLevel.DEFAULT)));
        verify(phoneNumberUtilMock).isValidNumber(Mockito.any());
        verify(phoneNumberUtilMock).parse(Mockito.any(), Mockito.any());
    }
}


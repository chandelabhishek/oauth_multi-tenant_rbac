package com.oauth.example.domain.validators;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.oauth.example.domain.annotations.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberValidator implements
        ConstraintValidator<PhoneNumber, String> {

    private PhoneNumberUtil phoneNumberUtil;

    public PhoneNumberValidator() {
    }

    public PhoneNumberValidator(PhoneNumberUtil phoneNumberUtil) {
        this.phoneNumberUtil = phoneNumberUtil;
    }

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        phoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    @Override
    public boolean isValid(String phoneNumber,
                           ConstraintValidatorContext cxt) {
        try {
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new IllegalArgumentException("phoneNumber is required");
            }
            var number = phoneNumberUtil.parse(phoneNumber,
                    Phonenumber.PhoneNumber.CountryCodeSource.UNSPECIFIED.name());
            return phoneNumberUtil.isValidNumber(number);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

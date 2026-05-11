package com.replyiq.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that transparently encrypts/decrypts string fields
 * when persisting to and reading from the database.
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final TokenEncryptor tokenEncryptor;

    public EncryptedStringConverter(TokenEncryptor tokenEncryptor) {
        this.tokenEncryptor = tokenEncryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return tokenEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return tokenEncryptor.decrypt(dbData);
    }
}

package com.monntterro.trelloflowbot.bot.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private final TextEncryptor encryptor;

    @Autowired
    public EncryptedStringConverter(@Value("${encryption.key}") String key,
                                    @Value("${encryption.salt}") String salt) {
        String hexKey = toHex(key);
        String hexSalt = toHex(salt);
        this.encryptor = Encryptors.text(hexKey, hexSalt);
    }

    private String toHex(String str) {
        return new String(Hex.encode(str.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute != null ? encryptor.encrypt(attribute) : null;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData != null ? encryptor.decrypt(dbData) : null;
    }
}

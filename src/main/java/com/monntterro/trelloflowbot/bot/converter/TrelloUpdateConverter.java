package com.monntterro.trelloflowbot.bot.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Component
@Converter
public class TrelloUpdateConverter implements AttributeConverter<TrelloUpdate, String> {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(TrelloUpdate attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TrelloUpdate convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, TrelloUpdate.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

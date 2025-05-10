package com.monntterro.trelloflowbot.bot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonParser {
    private static final ObjectMapper objectMapper;
    private final ObjectNode jsonNode;

    static {
        objectMapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        objectMapper.registerModule(timeModule);
    }

    private JsonParser() {
        this.jsonNode = objectMapper.createObjectNode();
    }

    public static JsonParser create() {
        return new JsonParser();
    }

    public JsonParser with(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key can not be null");
        }

        jsonNode.set(key, objectMapper.valueToTree(value));
        return this;
    }

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to String", e);
        }
    }

    public static <T> T read(String json, String name, Class<T> clazz) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return objectMapper.treeToValue(node.get(name), clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to read JSON from String", e);
        }
    }
}

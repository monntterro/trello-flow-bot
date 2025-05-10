package com.monntterro.trelloflowbot.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Type {
    COMMENT_CARD("commentCard"),
    UPDATE_CARD("updateCard"),
    UNKNOWN("unknown");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Type fromValue(String value) {
        for (Type type : Type.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}

package com.monntterro.trelloflowbot.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TranslationKey {
    ACTION_MOVE_CARD_FROM_LIST_TO_LIST("action_move_card_from_list_to_list"),
    ACTION_COMMENT_ON_CARD("action_comment_on_card"),
    UNKNOWN("unknown");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TranslationKey fromValue(String value) {
        for (TranslationKey translationKey : TranslationKey.values()) {
            if (translationKey.value.equalsIgnoreCase(value)) {
                return translationKey;
            }
        }
        return UNKNOWN;
    }
}

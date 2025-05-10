package com.monntterro.trelloflowbot.bot.model.callback;

import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallbackData {
    public CallbackType callbackType;
    public String data;

    public static CallbackData from(String data) {
        String callbackType = JsonParser.read(data, "type", String.class);
        CallbackType type = CallbackType.valueOf(callbackType);
        return new CallbackData(type, data);
    }
}

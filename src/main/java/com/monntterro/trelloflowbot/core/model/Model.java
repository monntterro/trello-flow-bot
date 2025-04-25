package com.monntterro.trelloflowbot.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
public class Model {
    @JsonInclude(content = JsonInclude.Include.NON_EMPTY)
    private Map<String, String> labelNames;
}

package com.monntterro.trelloflowbot.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {
    private String id;
    private String fullName;
    private String username;
}

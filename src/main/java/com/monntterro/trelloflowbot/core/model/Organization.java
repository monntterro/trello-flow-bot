package com.monntterro.trelloflowbot.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organization {
    private String id;
    private String url;
    private String displayName;
    private List<String> idBoards;
}

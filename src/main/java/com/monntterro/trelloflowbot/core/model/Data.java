package com.monntterro.trelloflowbot.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
    private String text;
    private Card card;
    private Board board;
    private List list;

    private List listBefore;
    private List listAfter;
}

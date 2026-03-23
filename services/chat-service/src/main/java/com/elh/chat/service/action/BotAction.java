package com.elh.chat.service.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BotAction(
        String command,
        Map<String, String> options
) {}

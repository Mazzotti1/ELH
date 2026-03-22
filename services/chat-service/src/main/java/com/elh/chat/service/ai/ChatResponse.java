package com.elh.chat.service.ai;

public record ChatResponse(String text, int inputTokens, int outputTokens) {}

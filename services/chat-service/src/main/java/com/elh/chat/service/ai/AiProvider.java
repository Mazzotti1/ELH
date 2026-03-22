package com.elh.chat.service.ai;

import java.util.List;
import java.util.Map;

public interface AiProvider {

    ChatResponse chat(List<Map<String, String>> conversationHistory);
}

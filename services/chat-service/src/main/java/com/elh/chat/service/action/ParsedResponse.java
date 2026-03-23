package com.elh.chat.service.action;

import java.util.List;

public record ParsedResponse(String text, List<BotAction> actions) {

    public boolean hasActions() {
        return actions != null && !actions.isEmpty();
    }
}

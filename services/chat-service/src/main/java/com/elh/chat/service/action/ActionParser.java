package com.elh.chat.service.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionParser {

    private static final Pattern ACTION_PATTERN =
            Pattern.compile("\\[ELH_ACTION](.*?)\\[/ELH_ACTION]", Pattern.DOTALL);

    private static final int MAX_ACTIONS = 2;

    private static final Set<String> ALLOWED_COMMANDS =
            Set.of("img", "buscar", "midia", "top", "stats", "poll", "historico", "tag");

    private final ObjectMapper objectMapper;

    public ParsedResponse parse(String aiResponse) {
        List<BotAction> actions = new ArrayList<>();
        Matcher matcher = ACTION_PATTERN.matcher(aiResponse);

        while (matcher.find() && actions.size() < MAX_ACTIONS) {
            String json = matcher.group(1).trim();
            try {
                BotAction action = objectMapper.readValue(json, BotAction.class);
                if (action.command() != null && ALLOWED_COMMANDS.contains(action.command())) {
                    actions.add(action);
                } else {
                    log.warn("Comando nao permitido ignorado: {}", action.command());
                }
            } catch (JsonProcessingException e) {
                log.warn("Falha ao parsear acao do bot: {}", json, e);
            }
        }

        String textResponse = ACTION_PATTERN.matcher(aiResponse).replaceAll("").trim();

        return new ParsedResponse(textResponse, actions);
    }
}

package com.elh.gateway.config;

import com.elh.gateway.listener.MessageListener;
import com.elh.gateway.listener.SlashCommandListener;
import com.elh.gateway.listener.ReactionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdaConfig {

    private final MessageListener messageListener;
    private final SlashCommandListener slashCommandListener;
    private final ReactionListener reactionListener;

    @Value("${discord.token}")
    private String token;

    @Bean
    public JDA jda() throws InterruptedException {
        log.info("Inicializando JDA...");

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(EnumSet.of(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.MESSAGE_CONTENT
                ))
                .disableCache(
                        CacheFlag.VOICE_STATE,
                        CacheFlag.EMOJI,
                        CacheFlag.STICKER,
                        CacheFlag.SCHEDULED_EVENTS
                )
                .addEventListeners(
                        messageListener,
                        slashCommandListener,
                        reactionListener
                )
                .build()
                .awaitReady();

        log.info("JDA pronto! Bot: {} | Guilds: {}", jda.getSelfUser().getName(), jda.getGuilds().size());
        return jda;
    }
}

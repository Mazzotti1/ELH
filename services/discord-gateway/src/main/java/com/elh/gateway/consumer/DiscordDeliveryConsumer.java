package com.elh.gateway.consumer;

import com.elh.gateway.consumer.dto.DiscordEmbedPayload;
import com.elh.gateway.consumer.dto.DiscordMessagePayload;
import com.elh.gateway.consumer.dto.DiscordPollPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.elh.commons.config.RabbitQueues.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordDeliveryConsumer {

    private final JDA jda;

    @RabbitListener(queues = Q_DISCORD_MESSAGE)
    public void handleMessage(DiscordMessagePayload payload) {
        log.debug("RabbitMQ -> discord.send.message para canal {}", payload.getChannelId());

        if (payload.getInteractionToken() != null) {
            replyToInteraction(payload.getInteractionToken(), payload.getContent());
        } else {
            sendToChannel(payload.getChannelId(), payload.getContent());
        }
    }

    @RabbitListener(queues = Q_DISCORD_EMBED)
    public void handleEmbed(DiscordEmbedPayload payload) {
        log.debug("RabbitMQ -> discord.send.embed para canal {}", payload.getChannelId());

        List<MessageEmbed> embeds = buildEmbeds(payload);

        if (payload.getInteractionToken() != null) {
            jda.retrieveApplicationInfo().queue(appInfo -> {
                InteractionHook.from(jda, payload.getInteractionToken())
                        .editOriginalEmbeds(embeds)
                        .queue(
                                ok -> log.debug("Embed enviado via interaction hook"),
                                err -> log.error("Falha ao enviar embed via hook: {}", err.getMessage())
                        );
            });
        } else {
            TextChannel channel = jda.getTextChannelById(payload.getChannelId());
            if (channel != null) {
                channel.sendMessageEmbeds(embeds).queue();
            }
        }
    }

    @RabbitListener(queues = Q_DISCORD_POLL)
    public void handlePoll(DiscordPollPayload payload) {
        log.debug("RabbitMQ -> discord.send.poll para canal {}", payload.getChannelId());

        TextChannel channel = jda.getTextChannelById(payload.getChannelId());
        if (channel == null) {
            log.warn("Canal {} nao encontrado para enviar poll", payload.getChannelId());
            return;
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🗳 " + payload.getTitle())
                .setColor(Color.decode("#f4c430"));

        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < payload.getOptions().size(); i++) {
            DiscordPollPayload.PollOption opt = payload.getOptions().get(i);
            desc.append(getNumberEmoji(i + 1)).append(" ").append(opt.getLabel()).append("\n");
        }
        eb.setDescription(desc.toString());

        channel.sendMessageEmbeds(eb.build()).queue(sentMsg -> {
            for (int i = 0; i < payload.getOptions().size(); i++) {
                sentMsg.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode(getNumberEmoji(i + 1))).queue();
            }
        });
    }

    private void replyToInteraction(String token, String content) {
        InteractionHook.from(jda, token)
                .editOriginal(content)
                .queue(
                        ok -> log.debug("Reply via interaction hook enviado"),
                        err -> log.error("Falha ao reply via hook: {}", err.getMessage())
                );
    }

    private void sendToChannel(String channelId, String content) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            channel.sendMessage(content).queue();
        } else {
            log.warn("Canal {} nao encontrado", channelId);
        }
    }

    private List<MessageEmbed> buildEmbeds(DiscordEmbedPayload payload) {
        List<MessageEmbed> embeds = new ArrayList<>();

        EmbedBuilder main = new EmbedBuilder();
        if (payload.getTitle() != null) main.setTitle(payload.getTitle());
        if (payload.getDescription() != null) main.setDescription(payload.getDescription());
        if (payload.getColor() != null) main.setColor(Color.decode(payload.getColor()));
        if (payload.getThumbnailUrl() != null) main.setThumbnail(payload.getThumbnailUrl());

        if (payload.getFields() != null) {
            payload.getFields().forEach(f ->
                    main.addField(f.getName(), f.getValue(), f.isInline()));
        }

        if (payload.getImageUrls() != null && !payload.getImageUrls().isEmpty()) {
            main.setImage(payload.getImageUrls().get(0));
            embeds.add(main.build());

            for (int i = 1; i < Math.min(payload.getImageUrls().size(), 4); i++) {
                embeds.add(new EmbedBuilder().setImage(payload.getImageUrls().get(i)).build());
            }
        } else {
            embeds.add(main.build());
        }

        return embeds;
    }

    private String getNumberEmoji(int n) {
        return switch (n) {
            case 1 -> "1️⃣";
            case 2 -> "2️⃣";
            case 3 -> "3️⃣";
            case 4 -> "4️⃣";
            case 5 -> "5️⃣";
            default -> "🔢";
        };
    }
}

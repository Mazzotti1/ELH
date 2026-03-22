package com.elh.gateway.listener;

import com.elh.commons.events.ReactionAddedEvent;
import com.elh.gateway.producer.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactionListener extends ListenerAdapter {

    private final EventPublisher publisher;

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getUser() != null && event.getUser().isBot()) return;

        ReactionAddedEvent reactionEvent = ReactionAddedEvent.builder()
                .guildId(event.getGuild().getId())
                .channelId(event.getChannel().getId())
                .messageId(event.getMessageId())
                .userId(event.getUserId())
                .emoji(event.getReaction().getEmoji().getAsReactionCode())
                .build();

        publisher.publishReactionAdded(reactionEvent);
        log.debug("Reaction {} no msg {} por {}", reactionEvent.getEmoji(), event.getMessageId(), event.getUserId());
    }
}

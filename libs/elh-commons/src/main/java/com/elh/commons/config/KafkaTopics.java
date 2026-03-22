package com.elh.commons.config;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String DISCORD_MESSAGES  = "discord.messages";
    public static final String DISCORD_COMMANDS  = "discord.commands";
    public static final String DISCORD_REACTIONS = "discord.reactions";
    public static final String MEDIA_DETECTED    = "media.detected";
    public static final String MEDIA_SAVED       = "media.saved";
    public static final String POLL_CREATED      = "poll.created";
    public static final String POLL_CLOSED       = "poll.closed";
    public static final String CHAT_REQUESTED    = "chat.requested";
    public static final String CHAT_RESPONDED    = "chat.responded";
}

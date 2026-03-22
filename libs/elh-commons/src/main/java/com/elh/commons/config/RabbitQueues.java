package com.elh.commons.config;

public final class RabbitQueues {

    private RabbitQueues() {}

    public static final String DISCORD_EXCHANGE  = "discord.exchange";
    public static final String DLX_EXCHANGE      = "dlx.exchange";

    public static final String RK_SEND_MESSAGE   = "discord.send.message";
    public static final String RK_SEND_EMBED     = "discord.send.embed";
    public static final String RK_SEND_POLL      = "discord.send.poll";
    public static final String RK_SEND_FILE      = "discord.send.file";

    public static final String Q_DISCORD_MESSAGE = "q.discord.message";
    public static final String Q_DISCORD_EMBED   = "q.discord.embed";
    public static final String Q_DISCORD_POLL    = "q.discord.poll";
    public static final String Q_DISCORD_FILE    = "q.discord.file";

    public static final String Q_DLQ_FAILED      = "q.dlq.failed";
}

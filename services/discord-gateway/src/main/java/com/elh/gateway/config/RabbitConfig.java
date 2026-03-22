package com.elh.gateway.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.elh.commons.config.RabbitQueues.*;

@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange discordExchange() {
        return new DirectExchange(DISCORD_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue qDiscordMessage() {
        return QueueBuilder.durable(Q_DISCORD_MESSAGE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_DLQ_FAILED)
                .build();
    }

    @Bean
    public Queue qDiscordEmbed() {
        return QueueBuilder.durable(Q_DISCORD_EMBED)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_DLQ_FAILED)
                .build();
    }

    @Bean
    public Queue qDiscordPoll() {
        return QueueBuilder.durable(Q_DISCORD_POLL)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_DLQ_FAILED)
                .build();
    }

    @Bean
    public Queue qDiscordFile() {
        return QueueBuilder.durable(Q_DISCORD_FILE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", Q_DLQ_FAILED)
                .build();
    }

    @Bean
    public Queue qDlqFailed() {
        return QueueBuilder.durable(Q_DLQ_FAILED).build();
    }

    @Bean
    public Binding bindMessage() {
        return BindingBuilder.bind(qDiscordMessage()).to(discordExchange()).with(RK_SEND_MESSAGE);
    }

    @Bean
    public Binding bindEmbed() {
        return BindingBuilder.bind(qDiscordEmbed()).to(discordExchange()).with(RK_SEND_EMBED);
    }

    @Bean
    public Binding bindPoll() {
        return BindingBuilder.bind(qDiscordPoll()).to(discordExchange()).with(RK_SEND_POLL);
    }

    @Bean
    public Binding bindFile() {
        return BindingBuilder.bind(qDiscordFile()).to(discordExchange()).with(RK_SEND_FILE);
    }

    @Bean
    public Binding bindDlq() {
        return BindingBuilder.bind(qDlqFailed()).to(dlxExchange()).with(Q_DLQ_FAILED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}

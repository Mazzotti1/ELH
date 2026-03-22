package com.elh.gateway.config;

import com.elh.commons.events.BaseEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static com.elh.commons.config.KafkaTopics.*;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, BaseEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                JsonSerializer.ADD_TYPE_INFO_HEADERS, false,
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.RETRIES_CONFIG, 3,
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
        ));
    }

    @Bean
    public KafkaTemplate<String, BaseEvent> kafkaTemplate(ProducerFactory<String, BaseEvent> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean public NewTopic topicDiscordMessages()  { return TopicBuilder.name(DISCORD_MESSAGES).partitions(3).replicas(1).build(); }
    @Bean public NewTopic topicDiscordCommands()   { return TopicBuilder.name(DISCORD_COMMANDS).partitions(3).replicas(1).build(); }
    @Bean public NewTopic topicDiscordReactions()  { return TopicBuilder.name(DISCORD_REACTIONS).partitions(3).replicas(1).build(); }
    @Bean public NewTopic topicMediaDetected()     { return TopicBuilder.name(MEDIA_DETECTED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic topicChatRequested()     { return TopicBuilder.name(CHAT_REQUESTED).partitions(3).replicas(1).build(); }
}

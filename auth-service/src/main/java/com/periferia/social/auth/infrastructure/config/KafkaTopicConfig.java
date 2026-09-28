package com.periferia.social.auth.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
class KafkaTopicConfig {

    @Bean
    NewTopic userRegisteredTopic(@Value("${app.kafka.topics.user-registered}") String name,
                                 @Value("${app.kafka.topic-partitions:3}") int partitions,
                                 @Value("${app.kafka.topic-replicas:1}") int replicas) {
        return TopicBuilder.name(name).partitions(partitions).replicas(replicas).build();
    }
}

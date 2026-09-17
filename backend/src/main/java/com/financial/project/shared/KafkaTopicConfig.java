package com.financial.project.shared;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declaring topics as beans lets Spring's KafkaAdmin provision them eagerly at
 * startup. Without this, a fresh broker (e.g. a Testcontainers instance in
 * tests) has no "payment-events" topic until the first producer send creates
 * it - and a consumer that subscribed just before that only notices the new
 * topic on its next metadata refresh (defaults to every 5 minutes), which
 * looks like the consumer silently never receiving anything.
 */
@Configuration
class KafkaTopicConfig {

    @Bean
    NewTopic paymentEventsTopic() {
        return TopicBuilder.name("payment-events").partitions(1).replicas(1).build();
    }
}

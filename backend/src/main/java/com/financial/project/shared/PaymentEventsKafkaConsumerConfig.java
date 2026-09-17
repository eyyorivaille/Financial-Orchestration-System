package com.financial.project.shared;

import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper;

/**
 * Shared listener container factory for every consumer of the "payment-events"
 * topic (Notification, Audit, ...). Each @KafkaListener sets its own groupId,
 * overriding whatever the underlying ConsumerFactory bean defaults to, so every
 * consumer still gets its own independent copy of every event.
 *
 * Deliberately NOT the global default RecordMessageConverter bean: doing that
 * once got wired into Spring Modulith's own Kafka producer too, breaking
 * Payment's event externalization ("Select a subclass that creates a
 * ProducerRecord..."). Scoping the converter to this named factory avoids that.
 * Without the type-aware JacksonJavaTypeMapper (TYPE_ID precedence, trusting
 * com.financial.project.payment), records deserialize to a generic LinkedHashMap
 * instead of the concrete event class, so @KafkaHandler's type dispatch silently
 * falls through to the default handler - no exception, no log, nothing.
 */
@Configuration
class PaymentEventsKafkaConsumerConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> paymentEventsKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);

        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.addTrustedPackages("com.financial.project.payment");
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.TYPE_ID);
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTypeMapper(typeMapper);
        factory.setRecordMessageConverter(converter);

        return factory;
    }
}

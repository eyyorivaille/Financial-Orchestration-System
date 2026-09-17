package com.financial.project.notification.internal;

import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper;

/**
 * A dedicated (not the default/shared) listener container factory, used only
 * by PaymentEventListener via @KafkaListener(containerFactory = ...). An
 * earlier attempt registered the JacksonJsonMessageConverter as the single
 * global RecordMessageConverter bean - Spring Boot's autoconfiguration then
 * also wired it into whatever Spring Modulith's own Kafka producer uses for
 * event externalization, breaking Payment's publish side ("Select a subclass
 * that creates a ProducerRecord..."). Scoping it to a named factory avoids that.
 */
@Configuration
class KafkaMessageConverterConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> notificationKafkaListenerContainerFactory(
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

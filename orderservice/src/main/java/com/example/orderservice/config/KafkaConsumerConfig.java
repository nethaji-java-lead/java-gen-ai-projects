package com.example.orderservice.config;

import com.example.orderservice.event.PaymentCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    /**
     * Kafka Listener Container Factory
     *
     * Flow:
     *
     * payment-created
     *       |
     *       v
     * PaymentEventConsumer
     *       |
     *       | exception
     *       v
     * Retry #1 -> 2 seconds
     *       |
     *       v
     * Retry #2 -> 2 seconds
     *       |
     *       v
     * DeadLetterPublishingRecoverer
     *       |
     *       v
     * payment-created.DLT
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String,
            PaymentCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentCreatedEvent> consumerFactory,
            KafkaTemplate<String, Object> dltKafkaTemplate) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<
                        String,
                        PaymentCreatedEvent>();

        factory.setConsumerFactory(consumerFactory);

        /*
         * Dead Letter Publishing Recoverer
         *
         * When all retries are exhausted, the failed
         * Kafka record is published to:
         *
         *     <original-topic>.DLT
         *
         * Example:
         *
         *     payment-created
         *              |
         *              v
         *     payment-created.DLT
         */
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        dltKafkaTemplate,
                        (record, exception) -> {

                            String dltTopic =
                                    record.topic() + ".DLT";

                            System.out.println(
                                    "========================================"
                            );

                            System.out.println(
                                    "Publishing record to DLT"
                            );

                            System.out.println(
                                    "Topic     : "
                                            + record.topic()
                            );

                            System.out.println(
                                    "Partition : "
                                            + record.partition()
                            );

                            System.out.println(
                                    "Offset    : "
                                            + record.offset()
                            );

                            System.out.println(
                                    "DLT       : "
                                            + dltTopic
                            );

                            System.out.println(
                                    "Exception : "
                                            + exception
                                            .getClass()
                                            .getSimpleName()
                            );

                            System.out.println(
                                    "========================================"
                            );

                            return new TopicPartition(
                                    dltTopic,
                                    record.partition()
                            );
                        }
                );

        /*
         * Make sure a failed DLT publish is propagated
         * rather than silently ignored.
         */
        recoverer.setFailIfSendResultIsError(true);

        /*
         * Retry configuration:
         *
         * Original attempt
         * Retry #1 -> after 2 seconds
         * Retry #2 -> after 2 seconds
         *
         * After that -> DLT
         */
        FixedBackOff backOff =
                new FixedBackOff(
                        2000L,
                        2L
                );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        recoverer,
                        backOff
                );

        factory.setCommonErrorHandler(
                errorHandler
        );

        return factory;
    }


    /**
     * ProducerFactory used by the
     * DeadLetterPublishingRecoverer.
     *
     * Key:
     *     String
     *
     * Value:
     *     JSON
     */
    @Bean
    public ProducerFactory<String, Object> dltProducerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> props =
                new HashMap<>(
                        kafkaProperties
                                .buildProducerProperties()
                );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(
                props
        );
    }


    /**
     * KafkaTemplate used exclusively by the
     * DeadLetterPublishingRecoverer.
     */
    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate(
            ProducerFactory<String, Object>
                    dltProducerFactory) {

        return new KafkaTemplate<>(
                dltProducerFactory
        );
    }


    /**
     * ProducerFactory for normal
     * PaymentCreatedEvent publishing.
     *
     * Used when publishing to:
     *
     *     payment-created
     */
    @Bean
    public ProducerFactory<String, PaymentCreatedEvent>
    paymentProducerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> props =
                new HashMap<>(
                        kafkaProperties
                                .buildProducerProperties()
                );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(
                props
        );
    }


    /**
     * KafkaTemplate used to publish
     * PaymentCreatedEvent.
     */
    @Bean
    public KafkaTemplate<String, PaymentCreatedEvent>
    paymentKafkaTemplate(
            ProducerFactory<String, PaymentCreatedEvent>
                    paymentProducerFactory) {

        return new KafkaTemplate<>(
                paymentProducerFactory
        );
    }
}
package com.jonathanfoucher.kafkaconsumer.config;

import com.jonathanfoucher.pojo.avro.movie.MovieKey;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@EnableKafka
@Configuration
@ConfigurationProperties("kafka-consumer")
@Getter
@Setter
public class KafkaConfig {
    private int retryInitialInterval;
    private int retryMultiplier;
    private int retryMaxInterval;

    @Bean
    public ConsumerFactory<MovieKey, MovieValue> consumerMovieFactory(KafkaProperties kafkaProperties, SslBundles sslBundles) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(sslBundles));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<MovieKey, MovieValue> kafkaListenerContainerMovieFactory(KafkaProperties kafkaProperties, SslBundles sslBundles) {
        ConcurrentKafkaListenerContainerFactory<MovieKey, MovieValue> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerMovieFactory(kafkaProperties, sslBundles));
        factory.getContainerProperties().setAckMode(kafkaProperties.getListener().getAckMode());
        factory.setCommonErrorHandler(customErrorHandler());
        return factory;
    }

    @Bean
    public DefaultErrorHandler customErrorHandler() {
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(retryInitialInterval);
        backOff.setMultiplier(retryMultiplier);
        backOff.setMaxInterval(retryMaxInterval);
        return new DefaultErrorHandler(backOff);
    }
}

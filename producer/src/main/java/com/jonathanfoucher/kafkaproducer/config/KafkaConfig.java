package com.jonathanfoucher.kafkaproducer.config;

import com.jonathanfoucher.pojo.avro.movie.MovieKey;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {
    @Bean
    public ProducerFactory<MovieKey, MovieValue> producerMovieFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<MovieKey, MovieValue> kafkaMovieTemplate(KafkaProperties kafkaProperties) {
        return new KafkaTemplate<>(producerMovieFactory(kafkaProperties));
    }
}

package com.jonathanfoucher.kafkaproducer.config;

import com.jonathanfoucher.pojo.avro.movie.MovieKey;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {
    @Bean
    public ProducerFactory<MovieKey, MovieValue> producerMovieFactory(KafkaProperties kafkaProperties, SslBundles sslBundles) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(sslBundles));
    }

    @Bean
    public KafkaTemplate<MovieKey, MovieValue> kafkaMovieTemplate(KafkaProperties kafkaProperties, SslBundles sslBundles) {
        return new KafkaTemplate<>(producerMovieFactory(kafkaProperties, sslBundles));
    }
}

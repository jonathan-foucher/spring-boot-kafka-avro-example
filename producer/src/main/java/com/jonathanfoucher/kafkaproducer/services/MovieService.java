package com.jonathanfoucher.kafkaproducer.services;

import com.jonathanfoucher.pojo.avro.movie.MovieKey;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {
    private final KafkaTemplate<MovieKey, MovieValue> kafkaTemplate;

    @Value("${spring.kafka.producer.topic-name}")
    private String MOVIE_TOPIC_NAME;

    public void saveMovie(MovieValue movieValue) {
        MovieKey movieKey = new MovieKey();
        movieKey.setId(movieValue.getId());
        sendToKafka(movieKey, movieValue);
    }

    public void deleteMovie(Long movieId) {
        MovieKey movieKey = new MovieKey();
        movieKey.setId(movieId);
        sendToKafka(movieKey, null);
    }

    private void sendToKafka(MovieKey movieKey, MovieValue movieValue) {
        final ProducerRecord<MovieKey, MovieValue> producerRecord = new ProducerRecord<>(MOVIE_TOPIC_NAME, movieKey, movieValue);

        try {
            kafkaTemplate.send(producerRecord);
            log.info("Sent record: key {} and value {}", producerRecord.key(), producerRecord.value());
        } catch (Exception e) {
            log.error("Failed to send record", e);
        }
    }
}

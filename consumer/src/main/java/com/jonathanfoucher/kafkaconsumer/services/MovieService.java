package com.jonathanfoucher.kafkaconsumer.services;

import com.jonathanfoucher.pojo.avro.movie.MovieKey;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {
    @KafkaListener(topics = "#{'${spring.kafka.consumer.topic-name}'}")
    public void consumeMovie(ConsumerRecord<MovieKey, MovieValue> consumerRecord, Acknowledgment ack) {
        log.info("Received record: key {} and value {}", consumerRecord.key(), consumerRecord.value());
        // uncomment to test the exponential error handler
        // if (true) throw new RuntimeException();
        ack.acknowledge();
    }
}

package com.jonathanfoucher.kafkaproducer.services;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jonathanfoucher.pojo.avro.movie.MovieKey;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(MovieService.class)
@SpringBootTest
class MovieServiceTest {
    @Autowired
    private MovieService movieService;
    @MockBean
    private KafkaTemplate<MovieKey, MovieValue> kafkaTemplate;

    private static final Logger log = (Logger) LoggerFactory.getLogger(MovieService.class);
    private static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @Value("${spring.kafka.producer.topic-name}")
    private String MOVIE_TOPIC_NAME;

    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);

    @BeforeEach
    void init() {
        listAppender.list.clear();
        listAppender.start();
        log.addAppender(listAppender);
    }

    @AfterEach
    void reset() {
        log.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void saveMovie() {
        // GIVEN
        MovieKey movieKey = initMovieKey();
        MovieValue movieValue = initMovieValue();

        // WHEN
        movieService.saveMovie(movieValue);

        // THEN
        ArgumentCaptor<ProducerRecord<MovieKey, MovieValue>> producerRecordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(producerRecordCaptor.capture());

        ProducerRecord<MovieKey, MovieValue> producerRecord = producerRecordCaptor.getValue();
        assertNotNull(producerRecord);
        assertEquals(MOVIE_TOPIC_NAME, producerRecord.topic());

        assertNotNull(producerRecord.key());
        assertEquals(ID, producerRecord.key().getId());

        assertNotNull(producerRecord.value());
        assertEquals(ID, producerRecord.value().getId());
        assertEquals(TITLE, producerRecord.value().getTitle());
        assertEquals(RELEASE_DATE, producerRecord.value().getReleaseDate());

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertNotNull(logsList.getFirst());
        assertEquals(Level.INFO, logsList.getFirst().getLevel());
        assertEquals(String.format("Sent record: key %s and value %s", movieKey, movieValue), logsList.getFirst().getFormattedMessage());
    }

    @Test
    void deleteMovie() {
        // GIVEN
        MovieKey movieKey = initMovieKey();

        // WHEN
        movieService.deleteMovie(ID);

        // THEN
        ArgumentCaptor<ProducerRecord<MovieKey, MovieValue>> producerRecordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(producerRecordCaptor.capture());

        ProducerRecord<MovieKey, MovieValue> producerRecord = producerRecordCaptor.getValue();
        assertNotNull(producerRecord);
        assertEquals(MOVIE_TOPIC_NAME, producerRecord.topic());

        assertNotNull(producerRecord.key());
        assertEquals(ID, producerRecord.key().getId());

        assertNull(producerRecord.value());

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertNotNull(logsList.getFirst());
        assertEquals(Level.INFO, logsList.getFirst().getLevel());
        assertEquals(String.format("Sent record: key %s and value %s", movieKey, null), logsList.getFirst().getFormattedMessage());
    }

    @Test
    void deleteMovieWithError() {
        // GIVEN
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenThrow(new RuntimeException());

        // WHEN
        movieService.deleteMovie(ID);

        // THEN
        ArgumentCaptor<ProducerRecord<MovieKey, MovieValue>> producerRecordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(producerRecordCaptor.capture());

        ProducerRecord<MovieKey, MovieValue> producerRecord = producerRecordCaptor.getValue();
        assertNotNull(producerRecord);
        assertEquals(MOVIE_TOPIC_NAME, producerRecord.topic());

        assertNotNull(producerRecord.key());
        assertEquals(ID, producerRecord.key().getId());

        assertNull(producerRecord.value());

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertNotNull(logsList.getFirst());
        assertEquals(Level.ERROR, logsList.getFirst().getLevel());
        assertEquals("Failed to send record", logsList.getFirst().getFormattedMessage());
    }

    private MovieKey initMovieKey() {
        MovieKey movieKey = new MovieKey();
        movieKey.setId(ID);
        return movieKey;
    }

    private MovieValue initMovieValue() {
        MovieValue movieValue = new MovieValue();
        movieValue.setId(ID);
        movieValue.setTitle(TITLE);
        movieValue.setReleaseDate(RELEASE_DATE);
        return movieValue;
    }
}

package com.jonathanfoucher.kafkaconsumer.services;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jonathanfoucher.pojo.avro.movie.MovieKey;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(MovieService.class)
@SpringBootTest
class MovieServiceTest {
    @Autowired
    private MovieService movieService;
    @MockBean
    private Acknowledgment acknowledgment;

    private static final Logger log = (Logger) LoggerFactory.getLogger(MovieService.class);
    private static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @Value("${spring.kafka.consumer.topic-name}")
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
    void consumeMovie() {
        // GIVEN
        MovieKey movieKey = initMovieKey();
        MovieValue movieValue = initMovieValue();
        ConsumerRecord<MovieKey, MovieValue> consumerRecord = new ConsumerRecord<>(MOVIE_TOPIC_NAME, 1, 1, movieKey, movieValue);

        // WHEN
        movieService.consumeMovie(consumerRecord, acknowledgment);

        // THEN
        verify(acknowledgment, times(1)).acknowledge();

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertNotNull(logsList.getFirst());
        assertEquals(Level.INFO, logsList.getFirst().getLevel());
        assertEquals(String.format("Received record: key %s and value %s", movieKey, movieValue), logsList.getFirst().getFormattedMessage());
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

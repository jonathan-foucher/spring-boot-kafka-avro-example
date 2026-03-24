package com.jonathanfoucher.kafkaproducer.controllers;

import com.jonathanfoucher.kafkaproducer.services.MovieService;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(MovieController.class)
class MovieControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private MovieController movieController;
    @MockitoBean
    private MovieService movieService;

    private static final String MOVIES_PATH = "/movies";
    private static final String MOVIES_WITH_ID_PATH = "/movies/{movie_id}";
    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);

    private static final JsonMapper jsonMapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    @BeforeEach
    void initEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(movieController)
                .setMessageConverters(new JacksonJsonHttpMessageConverter(jsonMapper))
                .build();
    }

    @Test
    void saveMovie() throws Exception {
        // GIVEN
        MovieValue movieValue = new MovieValue();
        movieValue.setId(ID);
        movieValue.setTitle(TITLE);
        movieValue.setReleaseDate(RELEASE_DATE);

        // WHEN / THEN
        mockMvc.perform(post(MOVIES_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(movieValue.toString())
                )
                .andExpect(status().isOk());

        ArgumentCaptor<MovieValue> movieValueCaptor = ArgumentCaptor.forClass(MovieValue.class);
        verify(movieService, times(1)).saveMovie(movieValueCaptor.capture());

        MovieValue savedMovieValue = movieValueCaptor.getValue();
        assertNotNull(savedMovieValue);
        assertEquals(ID, savedMovieValue.getId());
        assertEquals(TITLE, savedMovieValue.getTitle());
        assertEquals(RELEASE_DATE, savedMovieValue.getReleaseDate());
    }

    @Test
    void deleteMovie() throws Exception {
        // WHEN / THEN
        mockMvc.perform(delete(MOVIES_WITH_ID_PATH, ID))
                .andExpect(status().isOk());

        verify(movieService, times(1)).deleteMovie(ID);
    }
}

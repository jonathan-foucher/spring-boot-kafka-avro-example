package com.jonathanfoucher.kafkaproducer.controllers;

import com.jonathanfoucher.kafkaproducer.services.MovieService;
import com.jonathanfoucher.pojo.avro.movie.MovieValue;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    @PostMapping
    public void saveMovie(@RequestBody MovieValue movieValue) {
        movieService.saveMovie(movieValue);
    }

    @DeleteMapping("/{movie_id}")
    public void saveMovie(@PathVariable("movie_id") Long movieId) {
        movieService.deleteMovie(movieId);
    }
}

package com.geeky.redis;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private RedisMovieRepository redisMovieRepository;

    public MovieController(RedisMovieRepository redisMovieRepository) {
        this.redisMovieRepository = redisMovieRepository;
    }

    @PostMapping
    public void addMovie(@RequestBody String name) {
        redisMovieRepository.add(new Movie(UUID.randomUUID().toString(), name));
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable("id") String id) {
        redisMovieRepository.delete(id);
    }

    @GetMapping
    public List<Movie> getMovies() {
        Map<Object, Object> map = redisMovieRepository.findAllMovies();
        return map.entrySet().stream().map(e -> new Movie(e.getKey().toString(), e.getValue().toString()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Movie getMovie(@PathVariable("id") String id) {
        return redisMovieRepository.findMovie(id);
    }
}
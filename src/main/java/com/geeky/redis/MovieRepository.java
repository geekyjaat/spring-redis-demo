package com.geeky.redis;

import java.util.Map;

public interface MovieRepository {
    Map<Object, Object> findAllMovies();

    void add(Movie movie);

    void delete(String id);

    Movie findMovie(String id);
}
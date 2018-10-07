package com.geeky.redis;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest({ MovieController.class, RedisMovieRepository.class })
@AutoConfigureMockMvc(secure = false)
public class MovieControllerTests {

    @Autowired
    MockMvc mvc;

    @MockBean
    RedisMovieRepository redisMovieRepository;

    @Test
    public void testAdd() throws Exception {
        this.mvc.perform(post("/movies").content("movie")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testGetAll() throws Exception {
        Map<Object, Object> map = new HashMap<>();
        map.put("1", "movie1");
        map.put("2", "movie2");
        when(redisMovieRepository.findAllMovies()).thenReturn(map);
        this.mvc.perform(get("/movies")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo("1"))).andExpect(jsonPath("$[0].name", equalTo("movie1")))
                .andExpect(jsonPath("$[1].id", equalTo("2"))).andExpect(jsonPath("$[1].name", equalTo("movie2")));
    }

    @Test
    public void testGetOne() throws Exception {
        Movie movie = new Movie("1", "movie1");
        when(redisMovieRepository.findMovie("1")).thenReturn(movie);
        this.mvc.perform(get("/movies/1")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo("1"))).andExpect(jsonPath("$.name", equalTo("movie1")));
    }

    @Test
    public void testDelete() throws Exception {
        this.mvc.perform(delete("/movies/1")).andDo(print()).andExpect(status().isOk());
    }
}
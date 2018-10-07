# Spring Redis Demo

## Technologies used
1. [Spring Boot](https://spring.io/projects/spring-boot) Version - 2.0.5.RELEASE
2. [Redis](https://redis.io) 4.0.11
3. [Jedis](https://github.com/xetorthio/jedis) Client
4. [Docker](https://www.docker.com)

## Redis Docker
1. Install docker if you have not already.
2. Perform these commands to get a redis server up and running -

* `docker pull redis` - this will pull the official redis docker image.
* `docker run --name local-redis -p 6379:6379 redis` - this will start the redis server as docker container and expose its port `6379` to localhost `6379`.
> you can run `docker run --name local-redis -p 6379:6379 -d redis` to run the container as background daemon.

## The Spring part
1. Set up the dependencies in `build.gradle` -
    ```groovy
        implementation('org.springframework.boot:spring-boot-starter-data-redis')
        implementation('org.springframework.boot:spring-boot-starter-web')
        implementation('redis.clients:jedis:2.9.0')
    ```
2. With `Jedis` on classpath, we can set up the `RedisTemplate` bean - 
    ```java
        @Bean
        public JedisConnectionFactory jedisConnectionFactory() {
            return new JedisConnectionFactory();
        }
    
        @Bean
        public RedisTemplate<String, Object> redisTemplate() {
            final RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(jedisConnectionFactory());
            template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
            return template;
        }
    ```
3. if your server is running on different port or ip, use `RedisStandaloneConfiguration` to create the factory.
    ```java
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName("localhost");
            config.setPort(6397);
            return new JedisConnectionFactory(config);
    ```
4. We are going to use redis [Hashes](https://redis.io/topics/data-types-intro#hashes) to store our Movie records. It will achieved by implementing the repository for CRUD operations - 
    ```java
    @Repository
    public class RedisMovieRepository implements MovieRepository {
    
        private static final String KEY = "Movie";   
        private RedisTemplate<String, Object> redisTemplate;
        private HashOperations<String, Object, Object> hashOperations;
    
        public RedisMovieRepository(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }
    
        @PostConstruct
        private void init() {
            hashOperations = redisTemplate.opsForHash();
        }
    
        @Override
        public Map<Object, Object> findAllMovies() {
            return hashOperations.entries(KEY);
        }
    
        @Override
        public void add(Movie movie) {
            hashOperations.put(KEY, movie.getId(), movie.getName());
        }
    
        @Override
        public void delete(String id) {
            hashOperations.delete(KEY, id);
        }
    
        @Override
        public Movie findMovie(String id) {
            return (Movie) hashOperations.get(KEY, id);
        }
    
    }
    ```
5. Now that we are all set with our repository, we can add our controller to expose CRUD operations on HTTP - 
    ```java
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
    ```
    
6. Now we are all set. Let us fire up our application using `./gradlew bootRun`. Spring Boot will autoconfigure and will try to connect to redis on localhost.
    * Let us post a record - `curl -d "Blockbuster" -H "Content-Type: text/plain" -X POST http://localhost:8080/movies`
    * Let us get it back - `curl -X GET http://localhost:8080/movies` => `[{"id":"dbb29838-bd7a-49dd-9c78-ca2a53a62e94","name":"Blockbuster"}]`
    * Let us delete it - `curl -X DELETE http://localhost:8080/movies/dbb29838-bd7a-49dd-9c78-ca2a53a62e94`
    * Let us see if movie still present -  `curl -X GET http://localhost:8080/movies` => `[]`
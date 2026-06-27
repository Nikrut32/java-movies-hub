package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8081";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore moviesStore;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @BeforeAll
    static void beforeAll() throws IOException {
        moviesStore = new MoviesStore();
        server = new MoviesServer(moviesStore, 8081);
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        moviesStore.clearListOfMovies();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArrayTest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_whenNotEmpty_returnsArrayOfMoviesTest() throws Exception {
        Movie titanic = new Movie("Титаник", 1997);
        Movie ironManOne = new Movie("Железный человек 1", 2008);
        moviesStore.addMovie(titanic);
        moviesStore.addMovie(ironManOne);
        String moviesInJson = gson.toJson(moviesStore.createList(), new ListOfMoviesTypeToken().getType());
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(moviesInJson, resp.body(), "Ожидается не пустой JSON-массив");
    }

    @Test
    void postMovie_withValidPayload_returnsMovieTest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(BodyPublishers.ofString("{\"title\":\"Inception\",\"year\":2010}"))
                .headers("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "GET /movies должен вернуть 201");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertFalse(moviesStore.getListOfMovies().isEmpty(), "Фильм не был добавлен");
    }

    @Test
    void getMovieByIDTest() throws Exception {
        Movie titanic = new Movie("Титаник", 1997);
        Movie ironManOne = new Movie("Железный человек 1", 2008);
        moviesStore.addMovie(titanic);
        moviesStore.addMovie(ironManOne);

        String titanicJson = gson.toJson(titanic);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(titanicJson, resp.body(), "Вывелся другой фильм");

    }

    @Test
    void deleteMovieByIDTest() throws Exception {
        Movie titanic = new Movie("Титаник", 1997);
        Movie ironManOne = new Movie("Железный человек 1", 2008);
        moviesStore.addMovie(titanic);
        moviesStore.addMovie(ironManOne);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode(), "GET /movies должен вернуть 204");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(1, moviesStore.getListOfMovies().size());

    }

    @Test
    void getReturnListFilteredByYearTest() throws Exception {
        Movie titanic = new Movie("Титаник", 1997);
        Movie ironManOne = new Movie("Железный человек 1", 2008);
        Movie theIncredibleHulk = new Movie("Невероятный Халк", 2008);
        moviesStore.addMovie(titanic);
        moviesStore.addMovie(ironManOne);
        moviesStore.addMovie(theIncredibleHulk);

        List<Movie> movies2008 = new LinkedList<>(List.of(ironManOne, theIncredibleHulk));
        String moviesJson = gson.toJson(movies2008, new ListOfMoviesTypeToken().getType());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2008"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(moviesJson, resp.body(), "Фильмы неверно отфильтрованы");
    }
}
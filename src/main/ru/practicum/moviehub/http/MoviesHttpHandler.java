package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class MoviesHttpHandler extends BaseHttpHandler {
    private MoviesStore moviesStore;
    private ErrorResponse errorResponse;
    private Gson gson;

    MoviesHttpHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
        errorResponse = new ErrorResponse();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String path = exchange.getRequestURI().getPath();
        switch (exchange.getRequestMethod()) {
            case "GET":
                if (query != null) {
                    getFilteredByYear(exchange, query);
                } else if (path.split("/").length == 3) {
                    getMoviesByID(exchange, path);
                } else {
                    sendJson(exchange, 200, gson
                            .toJson(moviesStore.createList(), new ListOfMoviesTypeToken().getType()));
                }
                break;
            case "POST":
                postMovie(exchange);
                break;
            case "DELETE":
                deleteMovieByID(exchange, path);
                break;
            default:
                sendJson(exchange, 405, "");
                break;

        }
    }

    private void getMoviesByID(HttpExchange exchange, String path) throws IOException {
        errorResponse.setError("Ошибка поиска");
        try {
            int id = Integer.parseInt(path.split("/")[2]);
            if (moviesStore.getListOfMovies().containsKey(id)) {
                sendJson(exchange, 200, gson.toJson(moviesStore.getListOfMovies().get(id)));
            } else {
                errorResponse.addDetails("Фильм не найден");
                sendJson(exchange, 404, gson.toJson(errorResponse));
            }
        } catch (NumberFormatException e) {
            errorResponse.addDetails("Некорректный ID");
            sendJson(exchange, 400, gson.toJson(errorResponse));
        }
        errorResponse.clearDetails();
    }

    private void getFilteredByYear(HttpExchange exchange, String query) throws IOException {
        errorResponse.setError("Ошибка фильтрации");
        String stringYear = query.substring(query.indexOf("=") + 1);
        try {
            int year = Integer.parseInt(stringYear);
            if (year >= 1888 && year <= LocalDate.now().getYear()) {
                sendJson(exchange, 200, gson
                        .toJson(moviesStore.filterYear(year),
                                new ListOfMoviesTypeToken().getType()));
            } else {
                errorResponse.addDetails("Некорректный параметр запроса — " + stringYear);
            }
        } catch (NumberFormatException e) {
            errorResponse.addDetails("Некорректный параметр запроса — " + stringYear);
        }

        sendJson(exchange, 400, gson.toJson(errorResponse));
        errorResponse.clearDetails();
    }

    private void postMovie(HttpExchange exchange) throws IOException {
        errorResponse.setError("Ошибка добавления фильма");
        Headers requestHeaders = exchange.getRequestHeaders();

        if (!((requestHeaders.get("Content-type") != null)
                && (requestHeaders.get("Content-type").contains("application/json; charset=UTF-8")))) {
            sendJson(exchange, 415, "");
            return;
        }

        int intYear = 0;
        String title = "";
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String year = jsonObject.get("year").getAsString();
            title = jsonObject.get("title").getAsString();
            intYear = Integer.parseInt(year);
            if ((intYear >= 1888 && intYear <= LocalDate.now().getYear()) && !title.isBlank()) {
                Movie movie = new Movie(title, intYear);
                moviesStore.addMovie(movie);
                sendJson(exchange, 201, gson
                        .toJson(moviesStore.getListOfMovies().get(moviesStore.getCountId())));
                return;
            }

        } catch (JsonSyntaxException e) {
            errorResponse.setError("Невалидный JSON");
            sendJson(exchange, 400, gson.toJson(errorResponse));
            errorResponse.clearDetails();
            return;
        } catch (NumberFormatException e) {
            errorResponse.addDetails("В поле 'year' было введено не число");
        }

        if (intYear < 1888 || intYear > LocalDate.now().getYear()) {
            errorResponse.addDetails("Год должен быть между 1888 и 2026");
        }

        if (title.isBlank()) {
            errorResponse.addDetails("Название не должно быть пустым");
        }

        sendJson(exchange, 422, gson.toJson(errorResponse));
        errorResponse.clearDetails();
    }

    private void deleteMovieByID(HttpExchange exchange, String path) throws IOException {
        errorResponse.setError("Ошибка удаления");
        try {
            int id = Integer.parseInt(path.split("/")[2]);
            if (moviesStore.getListOfMovies().containsKey(id)) {
                moviesStore.deleteMovie(id);
                sendNoContent(exchange);
            } else {
                errorResponse.addDetails("Фильм не найден");
                sendJson(exchange, 404, gson.toJson(errorResponse));
            }
        } catch (NumberFormatException e) {
            errorResponse.addDetails("Некорректный ID");
            sendJson(exchange, 400, gson.toJson(errorResponse));
        }
        errorResponse.clearDetails();

    }
}


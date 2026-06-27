package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;
import java.util.stream.Collectors;

public class MoviesStore {
    private Map<Integer, Movie> listOfMovies;
    private int countId;

    public MoviesStore() {
        listOfMovies = new HashMap<>();
        countId = 0;
    }

    public void addMovie(Movie movie) {
        countId++;
        movie.setId(countId);
        listOfMovies.put(movie.getId(), movie);
    }

    public void clearListOfMovies() {
        listOfMovies.clear();
        countId = 0;
    }

    public void deleteMovie(int id) {
        listOfMovies.remove(id);
    }

    public List<Movie> createList() {
        return new ArrayList<>(listOfMovies.values());
    }

    public List<Movie> filterYear(int year) {
        return createList().stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }

    public Map<Integer, Movie> getListOfMovies() {
        return listOfMovies;
    }

    public int getCountId() {
        return countId;
    }
}
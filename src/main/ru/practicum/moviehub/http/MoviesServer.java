package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private HttpServer server;


    public MoviesServer(MoviesStore moviesStore, int i) throws IOException {
        server = HttpServer.create(new InetSocketAddress(i), 0);
        server.createContext("/movies", new MoviesHttpHandler(moviesStore));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(2);
    }
}
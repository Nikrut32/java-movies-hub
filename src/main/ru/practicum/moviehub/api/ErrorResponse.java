package ru.practicum.moviehub.api;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    private String error;
    private List<String> details;

    public ErrorResponse() {
        details = new ArrayList<>();
    }

    public void addDetails(String detail) {
        details.add(detail);
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void clearDetails() {
        details.clear();
    }
}
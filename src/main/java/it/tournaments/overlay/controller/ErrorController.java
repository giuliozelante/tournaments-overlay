package it.tournaments.overlay.controller;

import java.util.HashMap;
import java.util.Map;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;

@Controller("error")
public class ErrorController {
    
    @ExecuteOn(TaskExecutors.BLOCKING)
    @Produces(MediaType.TEXT_HTML)
    @View("error/notFound")
    @Error(status = HttpStatus.NOT_FOUND, global = true)
    public Map<String, Object> notFound(HttpRequest<?> request) {
        Map<String, Object> model = new HashMap<>();
        model.put("path", request.getPath());
        return model;
    }
    
    @ExecuteOn(TaskExecutors.BLOCKING)
    @Produces(MediaType.TEXT_HTML)
    @View("error/serverError")
    @Error(status = HttpStatus.INTERNAL_SERVER_ERROR, global = true)
    public Map<String, Object> serverError(HttpRequest<?> request, Throwable e) {
        Map<String, Object> model = new HashMap<>();
        model.put("path", request.getPath());
        model.put("error", e.getMessage());
        return model;
    }
} 
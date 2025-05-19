package it.tournaments.overlay.controller;

import java.util.HashMap;
import java.util.Map;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;

@Controller("/")
    public class HomeController {

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Produces(MediaType.TEXT_HTML)
    @View("index")
    @Get
    public Map<String, Object> index() {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Home");
        return model;
    }
} 
package it.tournaments.overlay.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;
import it.tournaments.overlay.model.Event;
import it.tournaments.overlay.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    
    private final EventService eventService;
    
    @Produces(MediaType.TEXT_HTML)
    @ExecuteOn(TaskExecutors.BLOCKING)
    @View("events/index")
    @Get
    public Map<String, Object> index(@QueryValue Long tournamentId) {
        log.info("Fetching events for tournament ID: {}", tournamentId);
        
        List<Event> events = eventService.getEventsByTournamentId(tournamentId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("events", events);
        model.put("tournamentId", tournamentId);
        
        return model;
    }
} 
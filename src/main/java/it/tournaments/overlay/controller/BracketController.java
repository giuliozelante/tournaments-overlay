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
import it.tournaments.overlay.model.BracketSet;
import it.tournaments.overlay.model.Event;
import it.tournaments.overlay.service.BracketService;
import it.tournaments.overlay.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller("/sets")
@RequiredArgsConstructor
@Slf4j
public class BracketController {
    
    private final BracketService bracketService;
    private final EventService eventService;
    
    @Produces(MediaType.TEXT_HTML)
    @ExecuteOn(TaskExecutors.BLOCKING)
    @View("sets/index")
    @Get
    public Map<String, Object> index(@QueryValue Long eventId, @QueryValue(defaultValue = "0") Long tournamentId) {
        log.info("Fetching bracket sets for event ID: {}", eventId);
        
        List<BracketSet> sets = bracketService.getSetsByEventId(eventId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("sets", sets);
        model.put("eventId", eventId);
        model.put("tournamentId", tournamentId);
        
        // Add event information if available
        if (tournamentId > 0) {
            try {
                List<Event> events = eventService.getEventsByTournamentId(tournamentId);
                Event event = events.stream()
                    .filter(e -> e.getId().equals(eventId))
                    .findFirst()
                    .orElse(null);
                
                if (event != null) {
                    model.put("event", event);
                }
            } catch (Exception e) {
                log.warn("Could not fetch event information", e);
            }
        }
        
        return model;
    }
} 
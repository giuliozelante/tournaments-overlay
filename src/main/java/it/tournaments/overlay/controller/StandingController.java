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
import it.tournaments.overlay.model.Standing;
import it.tournaments.overlay.service.StandingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller("/standings")
@RequiredArgsConstructor
@Slf4j
public class StandingController {
    
    private final StandingService standingService;
    
    @Produces(MediaType.TEXT_HTML)
    @ExecuteOn(TaskExecutors.BLOCKING)
    @View("standings/index")
    @Get
    public Map<String, Object> index(@QueryValue Long eventId,
                                     @QueryValue(defaultValue = "1") Integer page,
                                     @QueryValue(defaultValue = "20") Integer perPage) {
        log.info("Fetching standings for event ID: {}", eventId);
        
        List<Standing> standings = standingService.getStandingsByEventId(eventId, page, perPage);
        
        Map<String, Object> model = new HashMap<>();
        model.put("standings", standings);
        model.put("eventId", eventId);
        model.put("currentPage", page);
        model.put("perPage", perPage);
        
        return model;
    }
} 
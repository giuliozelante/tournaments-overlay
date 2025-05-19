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
import it.tournaments.overlay.model.Tournament;
import it.tournaments.overlay.service.TournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller("/tournaments")
@RequiredArgsConstructor
@Slf4j
public class TournamentController {
    
    private final TournamentService tournamentService;
    
    @Produces(MediaType.TEXT_HTML)
    @ExecuteOn(TaskExecutors.BLOCKING)
    @View("tournaments/index")
    @Get
    public Map<String, Object> index(@QueryValue(value = "ownerId", defaultValue = "1802421") String ownerId,
                                     @QueryValue(defaultValue = "1") Integer page,
                                     @QueryValue(defaultValue = "20") Integer perPage) {
        log.info("Fetching tournaments for owner ID: {} (page {}, perPage {})", ownerId, page, perPage);
        
        List<Tournament> tournaments = tournamentService.getTournaments(ownerId, page, perPage);
        
        Map<String, Object> model = new HashMap<>();
        model.put("tournaments", tournaments);
        model.put("currentPage", page);
        model.put("perPage", perPage);
        model.put("ownerId", ownerId);
        
        return model;
    }
} 
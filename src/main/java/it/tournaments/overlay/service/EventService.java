package it.tournaments.overlay.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.tournaments.overlay.client.StartGgClient;
import it.tournaments.overlay.client.response.EventsResponse;
import it.tournaments.overlay.model.Event;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final StartGgClient startGgClient;
    
    public List<Event> getEventsByTournamentId(Long tournamentId) {
        log.info("Fetching events for tournament ID: {}", tournamentId);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("tournamentId", tournamentId.toString());
        
        try {
            EventsResponse response = startGgClient.getEvents(variables);
            return mapToEvents(response);
        } catch (Exception e) {
            log.error("Error fetching events for tournament ID: {}", tournamentId, e);
            return new ArrayList<>();
        }
    }
    
    private List<Event> mapToEvents(EventsResponse response) {
        if (response == null || response.getData() == null || 
            response.getData().getTournament() == null || 
            response.getData().getTournament().getEvents() == null) {
            return new ArrayList<>();
        }
        
        return response.getData().getTournament().getEvents().stream()
            .map(event -> Event.builder()
                .id(Long.parseLong(event.getId()))
                .name(event.getName())
                .slug(event.getSlug())
                .numEntrants(event.getNumEntrants())
                .startAt(event.getStartAt() != null ? Instant.ofEpochSecond(event.getStartAt()) : null)
                .videogame(event.getVideogame() != null ? event.getVideogame().getName() : null)
                .build())
            .collect(Collectors.toList());
    }
} 
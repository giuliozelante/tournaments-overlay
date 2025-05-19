package it.tournaments.overlay.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.tournaments.overlay.client.StartGgClient;
import it.tournaments.overlay.client.response.StandingsResponse;
import it.tournaments.overlay.model.Standing;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class StandingService {

    private final StartGgClient startGgClient;
    
    public List<Standing> getStandingsByEventId(Long eventId, Integer page, Integer perPage) {
        log.info("Fetching standings for event ID: {} (page {}, perPage {})", eventId, page, perPage);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("eventId", eventId.toString());
        variables.put("page", page);
        variables.put("perPage", perPage);
        
        try {
            StandingsResponse response = startGgClient.getStandings(variables);
            return mapToStandings(response);
        } catch (Exception e) {
            log.error("Error fetching standings for event ID: {}", eventId, e);
            return new ArrayList<>();
        }
    }
    
    private List<Standing> mapToStandings(StandingsResponse response) {
        if (response == null || response.getData() == null || 
            response.getData().getEvent() == null || 
            response.getData().getEvent().getStandings() == null ||
            response.getData().getEvent().getStandings().getNodes() == null) {
            return new ArrayList<>();
        }
        
        return response.getData().getEvent().getStandings().getNodes().stream()
            .map(node -> {
                int wins = 0;
                int losses = 0;
                
                // Extract score if available
                if (node.getStats() != null && node.getStats().getScore() != null) {
                    String label = node.getStats().getScore().getLabel();
                    if (label != null && label.contains("-")) {
                        String[] parts = label.split("-");
                        if (parts.length == 2) {
                            try {
                                wins = Integer.parseInt(parts[0].trim());
                                losses = Integer.parseInt(parts[1].trim());
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse score: {}", label);
                            }
                        }
                    }
                }
                
                return Standing.builder()
                    .id(Long.valueOf(node.getId()))
                    .gamertag(node.getEntrant() != null ? node.getEntrant().getName() : null)
                    .placement(node.getPlacement())
                    .wins(wins)
                    .losses(losses)
                    .build();
            })
            .collect(Collectors.toList());
    }
} 
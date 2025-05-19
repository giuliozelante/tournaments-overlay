package it.tournaments.overlay.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.tournaments.overlay.client.StartGgClient;
import it.tournaments.overlay.client.response.TournamentsResponse;
import it.tournaments.overlay.model.Tournament;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class TournamentService {
    
    private final StartGgClient startGgClient;
    
    public List<Tournament> getTournaments(String ownerId, Integer page, Integer perPage) {
        log.info("Fetching tournaments for owner ID: {} (page {}, perPage {})", ownerId, page, perPage);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("ownerId", ownerId);
        variables.put("page", page);
        variables.put("perPage", perPage);
        
        try {
            TournamentsResponse response = startGgClient.getTournaments(variables);
            return mapToTournaments(response);
        } catch (Exception e) {
            log.error("Error fetching tournaments", e);
            return new ArrayList<>();
        }
    }
    
    private List<Tournament> mapToTournaments(TournamentsResponse response) {
        if (response == null || response.getData() == null || 
            response.getData().getTournaments() == null || 
            response.getData().getTournaments().getNodes() == null) {
            return new ArrayList<>();
        }
        
        return response.getData().getTournaments().getNodes().stream()
            .map(node -> Tournament.builder()
                .id(Long.parseLong(node.getId()))
                .name(node.getName())
                .slug(node.getSlug())
                .city(node.getCity())
                .countryCode(node.getCountryCode())
                .startAt(node.getStartAt() != null ? Instant.ofEpochSecond(node.getStartAt()) : null)
                .endAt(node.getEndAt() != null ? Instant.ofEpochSecond(node.getEndAt()) : null)
                .timezone(node.getTimezone())
                .imageUrl(node.getImages() != null && !node.getImages().isEmpty() ? 
                    node.getImages().get(0).getUrl() : null)
                .build())
            .collect(Collectors.toList());
    }
} 
package it.tournaments.overlay.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.tournaments.overlay.client.StartGgClient;
import it.tournaments.overlay.client.response.StandingsResponse;
import it.tournaments.overlay.client.response.StandingsResponse.Phase;
import it.tournaments.overlay.client.response.StandingsResponse.PhaseGroup;
import it.tournaments.overlay.client.response.StandingsResponse.StandingNode;
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
            return mapToStandings(response, eventId);
        } catch (Exception e) {
            log.error("Error fetching standings for event ID: {}", eventId, e);
            return new ArrayList<>();
        }
    }
    
    private List<Standing> mapToStandings(StandingsResponse response, Long eventId) {
        if (response == null || response.getData() == null || 
            response.getData().getEvent() == null || 
            response.getData().getEvent().getPhases() == null) {
            return new ArrayList<>();
        }
        
        List<Standing> allStandings = new ArrayList<>();
        
        for (Phase phase : response.getData().getEvent().getPhases()) {
            if (phase.getPhaseGroups() == null || phase.getPhaseGroups().getNodes() == null) {
                continue;
            }
            
            for (PhaseGroup phaseGroup : phase.getPhaseGroups().getNodes()) {
                if (phaseGroup.getStandings() == null || phaseGroup.getStandings().getNodes() == null) {
                    continue;
                }
                
                List<StandingNode> standingNodes = phaseGroup.getStandings().getNodes();
                
                for (StandingNode node : standingNodes) {
                    if (node.getEntrant() == null) {
                        continue;
                    }
                    
                    int wins = 0;
                    int losses = 0;
                    
                    // Extract win/loss record from score label (e.g., "2-1")
                    if (node.getStats() != null && node.getStats().getScore() != null 
                        && node.getStats().getScore().getLabel() != null) {
                        String scoreLabel = node.getStats().getScore().getLabel();
                        if (scoreLabel.contains("-")) {
                            try {
                                String[] parts = scoreLabel.split("-");
                                if (parts.length == 2) {
                                    wins = Integer.parseInt(parts[0].trim());
                                    losses = Integer.parseInt(parts[1].trim());
                                }
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse score label: {}", node.getStats().getScore().getLabel());
                            }
                        }
                    }
                    
                    // Extract player name with prefix if available
                    String gamertag = node.getEntrant().getName();
                    if (node.getEntrant().getParticipants() != null && 
                        !node.getEntrant().getParticipants().isEmpty() && 
                        node.getEntrant().getParticipants().get(0) != null &&
                        node.getEntrant().getParticipants().get(0).getPlayer() != null) {
                        
                        var player = node.getEntrant().getParticipants().get(0).getPlayer();
                        if (player.getPrefix() != null && !player.getPrefix().isEmpty()) {
                            gamertag = player.getPrefix() + " | " + player.getGamerTag();
                        } else if (player.getGamerTag() != null) {
                            gamertag = player.getGamerTag();
                        }
                    }
                    
                    Standing standing = Standing.builder()
                        .id(Long.valueOf(node.getId()))
                        .gamertag(gamertag)
                        .placement(node.getPlacement())
                        .wins(wins)
                        .losses(losses)
                        .eventId(eventId)
                        .build();
                        
                    allStandings.add(standing);
                }
            }
        }
        
        return allStandings;
    }
} 
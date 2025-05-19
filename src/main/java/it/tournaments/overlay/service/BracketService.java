package it.tournaments.overlay.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.tournaments.overlay.client.StartGgClient;
import it.tournaments.overlay.client.response.SetsResponse;
import it.tournaments.overlay.model.BracketSet;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class BracketService {

    private final StartGgClient startGgClient;
    
    public List<BracketSet> getSetsByEventId(Long eventId) {
        log.info("Fetching bracket sets for event ID: {}", eventId);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("eventId", eventId.toString());
        
        try {
            SetsResponse response = startGgClient.getSets(variables);
            return mapToBracketSets(response);
        } catch (Exception e) {
            log.error("Error fetching bracket sets for event ID: {}", eventId, e);
            return new ArrayList<>();
        }
    }
    
    private List<BracketSet> mapToBracketSets(SetsResponse response) {
        if (response == null || response.getData() == null || 
            response.getData().getEvent() == null || 
            response.getData().getEvent().getSets() == null ||
            response.getData().getEvent().getSets().getNodes() == null) {
            return new ArrayList<>();
        }
        
        return response.getData().getEvent().getSets().getNodes().stream()
            .filter(node -> node.getSlots() != null && node.getSlots().size() >= 2)
            .map(node -> {
                // Extract scores and player info
                String player1Name = null;
                Integer player1Score = null;
                String player1Status = null;
                
                String player2Name = null;
                Integer player2Score = null;
                String player2Status = null;
                
                if (node.getSlots().size() >= 2) {
                    var slot1 = node.getSlots().get(0);
                    var slot2 = node.getSlots().get(1);
                    
                    // Player 1
                    if (slot1.getEntrant() != null) {
                        player1Name = slot1.getEntrant().getName();
                    }
                    if (slot1.getStanding() != null) {
                        player1Status = slot1.getStanding().getPlacement() == 1 ? "winner" : "loser";
                        if (slot1.getStanding().getStats() != null && 
                            slot1.getStanding().getStats().getScore() != null) {
                            player1Score = slot1.getStanding().getStats().getScore().getValue();
                        }
                    }
                    
                    // Player 2
                    if (slot2.getEntrant() != null) {
                        player2Name = slot2.getEntrant().getName();
                    }
                    if (slot2.getStanding() != null) {
                        player2Status = slot2.getStanding().getPlacement() == 1 ? "winner" : "loser";
                        if (slot2.getStanding().getStats() != null && 
                            slot2.getStanding().getStats().getScore() != null) {
                            player2Score = slot2.getStanding().getStats().getScore().getValue();
                        }
                    }
                }
                
                // Safely parse ID - if it's not a valid number, use a hash code instead
                Long id;
                try {
                    id = Long.valueOf(node.getId());
                } catch (NumberFormatException e) {
                    // Use hashCode as a fallback for non-numeric IDs
                    id = (long) node.getId().hashCode();
                    log.debug("Non-numeric ID found: {}, using hash: {}", node.getId(), id);
                }
                
                return BracketSet.builder()
                    .id(id)
                    .fullRoundText(node.getFullRoundText())
                    .round(node.getRound().toString())
                    .startAt(node.getStartAt() != null ? Instant.ofEpochSecond(node.getStartAt()) : null)
                    .state(node.getState())
                    .player1Name(player1Name)
                    .player1Score(player1Score)
                    .player1Status(player1Status)
                    .player2Name(player2Name)
                    .player2Score(player2Score)
                    .player2Status(player2Status)
                    .build();
            })
            .collect(Collectors.toList());
    }
} 
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
            List<BracketSet> sets = mapToBracketSets(response);
            
            // Calculate bracket progression
            calculateBracketProgression(sets);
            
            return sets;
        } catch (Exception e) {
            log.error("Error fetching bracket sets for event ID: {}", eventId, e);
            return new ArrayList<>();
        }
    }
    
    private void calculateBracketProgression(List<BracketSet> sets) {
        log.info("Calculating bracket progression for {} sets", sets.size());
        
        // Separate winners and losers bracket sets
        List<BracketSet> winnersSets = sets.stream()
            .filter(set -> !set.getFullRoundText().toLowerCase().contains("loser"))
            .collect(Collectors.toList());
            
        List<BracketSet> losersSets = sets.stream()
            .filter(set -> set.getFullRoundText().toLowerCase().contains("loser"))
            .collect(Collectors.toList());
        
        log.info("Winners bracket sets: {}, Losers bracket sets: {}", winnersSets.size(), losersSets.size());
        
        // Log round structure
        logRoundStructure("Winners", winnersSets);
        logRoundStructure("Losers", losersSets);
        
        // Calculate winners bracket progression
        calculateWinnersBracketProgression(winnersSets, losersSets);
        
        // Calculate losers bracket progression
        calculateLosersBracketProgression(losersSets);
        
        // Log final progression
        logProgression(sets);
    }
    
    private void calculateWinnersBracketProgression(List<BracketSet> winnersSets, List<BracketSet> losersSets) {
        // Group by round and sort by set ID within each round
        Map<String, List<BracketSet>> roundGroups = winnersSets.stream()
            .collect(Collectors.groupingBy(BracketSet::getFullRoundText));
        
        List<String> sortedRounds = roundGroups.keySet().stream()
            .sorted((r1, r2) -> {
                // Sort by the first set ID in each round (assuming earlier rounds have lower IDs)
                Long id1 = roundGroups.get(r1).stream().mapToLong(BracketSet::getId).min().orElse(Long.MAX_VALUE);
                Long id2 = roundGroups.get(r2).stream().mapToLong(BracketSet::getId).min().orElse(Long.MAX_VALUE);
                return Long.compare(id1, id2);
            })
            .collect(Collectors.toList());
        
        // For each round, calculate where winners advance
        for (int i = 0; i < sortedRounds.size() - 1; i++) {
            String currentRound = sortedRounds.get(i);
            String nextRound = sortedRounds.get(i + 1);
            
            List<BracketSet> currentSets = roundGroups.get(currentRound);
            List<BracketSet> nextSets = roundGroups.get(nextRound);
            
            // Sort sets within each round by ID for consistent pairing
            currentSets.sort((s1, s2) -> Long.compare(s1.getId(), s2.getId()));
            nextSets.sort((s1, s2) -> Long.compare(s1.getId(), s2.getId()));
            
            // Create proper bracket progression: each set should have unique destinations
            // In a proper bracket, sets are paired and each pair feeds into one next set
            for (int j = 0; j < currentSets.size(); j++) {
                BracketSet currentSet = currentSets.get(j);
                
                // Calculate which next set this current set feeds into
                // In a standard bracket: sets 0,1 → next set 0; sets 2,3 → next set 1; etc.
                int nextSetIndex = j / 2;
                
                if (nextSetIndex < nextSets.size()) {
                    BracketSet nextSet = nextSets.get(nextSetIndex);
                    currentSet.setWinnersNextSetId(nextSet.getId().toString());
                } else {
                    // This set doesn't advance (might be final)
                    currentSet.setWinnersNextSetId(null);
                }
                
                // For losers destination, use a more sophisticated approach
                findLosersDestination(currentSet, losersSets, i, j);
            }
        }
    }
    
    private void calculateLosersBracketProgression(List<BracketSet> losersSets) {
        if (losersSets.isEmpty()) return;
        
        // Group by round and sort
        Map<String, List<BracketSet>> roundGroups = losersSets.stream()
            .collect(Collectors.groupingBy(BracketSet::getFullRoundText));
        
        List<String> sortedRounds = roundGroups.keySet().stream()
            .sorted((r1, r2) -> {
                Long id1 = roundGroups.get(r1).stream().mapToLong(BracketSet::getId).min().orElse(Long.MAX_VALUE);
                Long id2 = roundGroups.get(r2).stream().mapToLong(BracketSet::getId).min().orElse(Long.MAX_VALUE);
                return Long.compare(id1, id2);
            })
            .collect(Collectors.toList());
        
        // For each round, calculate where winners advance in losers bracket
        for (int i = 0; i < sortedRounds.size() - 1; i++) {
            String currentRound = sortedRounds.get(i);
            String nextRound = sortedRounds.get(i + 1);
            
            List<BracketSet> currentSets = roundGroups.get(currentRound);
            List<BracketSet> nextSets = roundGroups.get(nextRound);
            
            // Sort sets within each round by ID
            currentSets.sort((s1, s2) -> Long.compare(s1.getId(), s2.getId()));
            nextSets.sort((s1, s2) -> Long.compare(s1.getId(), s2.getId()));
            
            // In losers bracket, winners advance, losers are eliminated
            for (int j = 0; j < currentSets.size() && j / 2 < nextSets.size(); j++) {
                BracketSet currentSet = currentSets.get(j);
                BracketSet nextSet = nextSets.get(j / 2);
                
                // Winner advances in losers bracket
                currentSet.setWinnersNextSetId(nextSet.getId().toString());
                
                // Loser is eliminated
                currentSet.setLosersNextSetId("ELIMINATED");
            }
        }
    }
    
    private void findLosersDestination(BracketSet winnersSet, List<BracketSet> losersSets, int winnersRoundIndex, int setIndexInRound) {
        if (losersSets.isEmpty()) {
            winnersSet.setLosersNextSetId("ELIMINATED");
            return;
        }
        
        // Group losers sets by round
        Map<String, List<BracketSet>> losersRoundGroups = losersSets.stream()
            .collect(Collectors.groupingBy(BracketSet::getFullRoundText));
        
        List<String> sortedLosersRounds = losersRoundGroups.keySet().stream()
            .sorted((r1, r2) -> {
                Long id1 = losersRoundGroups.get(r1).stream().mapToLong(BracketSet::getId).min().orElse(Long.MAX_VALUE);
                Long id2 = losersRoundGroups.get(r2).stream().mapToLong(BracketSet::getId).min().orElse(Long.MAX_VALUE);
                return Long.compare(id1, id2);
            })
            .collect(Collectors.toList());
        
        // More sophisticated losers bracket mapping
        // Different winners rounds drop to different losers rounds
        if (winnersRoundIndex < sortedLosersRounds.size()) {
            String targetLosersRound = sortedLosersRounds.get(winnersRoundIndex);
            List<BracketSet> targetRoundSets = losersRoundGroups.get(targetLosersRound);
            targetRoundSets.sort((s1, s2) -> Long.compare(s1.getId(), s2.getId()));
            
            // Map to specific set within the losers round based on position
            int targetSetIndex = setIndexInRound % targetRoundSets.size();
            if (targetSetIndex < targetRoundSets.size()) {
                BracketSet targetSet = targetRoundSets.get(targetSetIndex);
                winnersSet.setLosersNextSetId(targetSet.getId().toString());
            } else {
                winnersSet.setLosersNextSetId("ELIMINATED");
            }
        } else {
            winnersSet.setLosersNextSetId("ELIMINATED");
        }
    }
    
    private void logRoundStructure(String bracketType, List<BracketSet> sets) {
        Map<String, List<BracketSet>> rounds = sets.stream()
            .collect(Collectors.groupingBy(BracketSet::getFullRoundText));
        
        log.info("{} bracket rounds:", bracketType);
        rounds.entrySet().stream()
            .sorted((e1, e2) -> {
                Long id1 = e1.getValue().stream().mapToLong(BracketSet::getId).min().orElse(Long.MAX_VALUE);
                Long id2 = e2.getValue().stream().mapToLong(BracketSet::getId).min().orElse(Long.MAX_VALUE);
                return Long.compare(id1, id2);
            })
            .forEach(entry -> {
                String roundName = entry.getKey();
                List<Long> setIds = entry.getValue().stream()
                    .map(BracketSet::getId)
                    .sorted()
                    .collect(Collectors.toList());
                log.info("  {}: {}", roundName, setIds);
            });
    }
    
    private void logProgression(List<BracketSet> sets) {
        log.info("Bracket progression summary:");
        sets.forEach(set -> {
            if (set.getWinnersNextSetId() != null || set.getLosersNextSetId() != null) {
                log.info("Set {}: Winner→{}, Loser→{}", 
                    set.getId(), 
                    set.getWinnersNextSetId(), 
                    set.getLosersNextSetId());
            }
        });
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
                    .winnerId(node.getWinnerId())
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
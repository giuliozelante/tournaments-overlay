package it.tournaments.overlay.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BracketSet {
    private Long id;
    private String fullRoundText;
    private String round;
    private Instant startAt;
    private String state;
    
    private String player1Name;
    private Integer player1Score;
    private String player1Status;
    
    private String player2Name;
    private Integer player2Score;
    private String player2Status;
    
    private Long eventId;
} 
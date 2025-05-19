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
public class Event {
    private Long id;
    private String name;
    private String slug;
    private Integer numEntrants;
    private Instant startAt;
    private String videogame;
    private Long tournamentId;
} 
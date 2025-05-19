package it.tournaments.overlay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Standing {
    private Long id;
    private String gamertag;
    private Integer placement;
    private Integer wins;
    private Integer losses;
    private Long eventId;
} 
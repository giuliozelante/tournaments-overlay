package it.tournaments.overlay.model;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    private Long id;
    private String name;
    private String slug;
    private String city;
    private String countryCode;
    private Instant startAt;
    private Instant endAt;
    private String timezone;
    private String imageUrl;
    private List<Event> events;
} 
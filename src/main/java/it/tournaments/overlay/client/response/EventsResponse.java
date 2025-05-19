package it.tournaments.overlay.client.response;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
public class EventsResponse {
    private EventsData data;

    @Data
    @Serdeable
    public static class EventsData {
        private Tournament tournament;
    }

    @Data
    @Serdeable
    public static class Tournament {
        private List<Event> events;
    }

    @Data
    @Serdeable
    public static class Event {
        private String id;
        private String name;
        private String slug;
        private Integer numEntrants;
        private Long startAt;
        private Videogame videogame;
    }

    @Data
    @Serdeable
    public static class Videogame {
        private String name;
    }
} 
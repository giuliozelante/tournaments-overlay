package it.tournaments.overlay.client.response;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
public class StandingsResponse {
    private StandingsData data;

    @Data
    @Serdeable
    public static class StandingsData {
        private Event event;
    }

    @Data
    @Serdeable
    public static class Event {
        private StandingConnection standings;
    }

    @Data
    @Serdeable
    public static class StandingConnection {
        private List<StandingNode> nodes;
    }

    @Data
    @Serdeable
    public static class StandingNode {
        private String id;
        private Integer placement;
        private Entrant entrant;
        private Stats stats;
    }

    @Data
    @Serdeable
    public static class Entrant {
        private String name;
    }

    @Data
    @Serdeable
    public static class Stats {
        private Score score;
    }

    @Data
    @Serdeable
    public static class Score {
        private String label;
        private Integer value;
    }
} 
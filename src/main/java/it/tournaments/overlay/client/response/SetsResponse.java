package it.tournaments.overlay.client.response;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
public class SetsResponse {
    private SetsData data;

    @Data
    @Serdeable
    public static class SetsData {
        private Event event;
    }

    @Data
    @Serdeable
    public static class Event {
        private SetConnection sets;
    }

    @Data
    @Serdeable
    public static class SetConnection {
        private List<SetNode> nodes;
    }

    @Data
    @Serdeable
    public static class SetNode {
        private String id;
        private String fullRoundText;
        private Integer round;
        private Long startAt;
        private String state;
        private List<Slot> slots;
    }

    @Data
    @Serdeable
    public static class Slot {
        private Standing standing;
        private Entrant entrant;
    }

    @Data
    @Serdeable
    public static class Standing {
        private Integer placement;
        private Stats stats;
    }

    @Data
    @Serdeable
    public static class Stats {
        private Score score;
    }

    @Data
    @Serdeable
    public static class Score {
        private Integer value;
    }

    @Data
    @Serdeable
    public static class Entrant {
        private String name;
    }
} 
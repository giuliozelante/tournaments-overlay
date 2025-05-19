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
        private String id;
        private String name;
        private Tournament tournament;
        private List<Phase> phases;
    }

    @Data
    @Serdeable
    public static class Tournament {
        private String id;
    }
    
    @Data
    @Serdeable
    public static class Phase {
        private String id;
        private String name;
        private PhaseGroupConnection phaseGroups;
    }
    
    @Data
    @Serdeable
    public static class PhaseGroupConnection {
        private List<PhaseGroup> nodes;
    }
    
    @Data
    @Serdeable
    public static class PhaseGroup {
        private String id;
        private String displayIdentifier;
        private String bracketType;
        private StandingConnection standings;
    }
    
    @Data
    @Serdeable
    public static class StandingConnection {
        private List<StandingNode> nodes;
        private PageInfo pageInfo;
    }

    @Data
    @Serdeable
    public static class PageInfo {
        private Integer page;
        private Integer total;
        private Integer perPage;
        private Integer totalPages;
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
        private String id;
        private String name;
        private List<Participant> participants;
    }
    
    @Data
    @Serdeable
    public static class Participant {
        private Player player;
    }
    
    @Data
    @Serdeable
    public static class Player {
        private String id;
        private String gamerTag;
        private String prefix;
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
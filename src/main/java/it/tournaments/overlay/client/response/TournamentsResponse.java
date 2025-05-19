package it.tournaments.overlay.client.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

@Data
@Serdeable
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentsResponse {
    private TournamentsData data;

    @Data
    @Serdeable
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TournamentsData {
        private TournamentConnection tournaments;
    }

    @Data
    @Serdeable
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TournamentConnection {
        private List<TournamentNode> nodes;
    }

    @Data
    @Serdeable
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TournamentNode {
        private String id;
        private String name;
        private String slug;
        private String city;
        private String countryCode;
        private Long startAt;
        private Long endAt;
        private String timezone;
        private List<Image> images;
    }

    @Data
    @Serdeable
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String url;
    }
} 
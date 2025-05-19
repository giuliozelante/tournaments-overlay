package it.tournaments.overlay.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("graphql") // Matches "graphql." prefix in properties
public interface GraphQLConfig {
  String getEndpoint();

  String getToken();
}

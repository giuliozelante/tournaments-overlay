package it.tournaments.overlay.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class JacksonConfig {
    
    @Singleton
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Configure to ignore unknown properties to handle GraphQL extensions
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
} 
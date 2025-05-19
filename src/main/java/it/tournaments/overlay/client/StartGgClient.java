package it.tournaments.overlay.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.tournaments.overlay.client.response.EventsResponse;
import it.tournaments.overlay.client.response.SetsResponse;
import it.tournaments.overlay.client.response.StandingsResponse;
import it.tournaments.overlay.client.response.TournamentsResponse;
import it.tournaments.overlay.config.GraphQLConfig;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
@Slf4j
public class StartGgClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String API_URL = "https://api.start.gg/gql/alpha";
    
    private final GraphQLConfig graphQLConfig;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public StartGgClient(GraphQLConfig graphQLConfig, ObjectMapper objectMapper) {
        this.graphQLConfig = graphQLConfig;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
            
        // Log token information for debugging
        String token = graphQLConfig.getToken();
        if (token == null || token.isEmpty()) {
            log.error("API Token is missing! Please set GRAPHQL_TOKEN environment variable");
        } else {
            log.debug("API Token is configured (length: {})", token.length());
        }
    }

    public TournamentsResponse getTournaments(Map<String, Object> variables) {
        log.debug("Fetching tournaments with variables: {}", variables);
        
        // Ensure ownerId is present
        if (!variables.containsKey("ownerId")) {
            throw new IllegalArgumentException("ownerId is required for tournaments query");
        }
        
        try {
            String query = loadQueryFromFile("tournaments/Tournaments.graphql");
            log.debug("Using GraphQL query: {}", query);
            
            // Since we renamed the query in the .graphql file, update the operation name here
            variables.put("operationName", "TournamentsQuery");
            
            JsonNode responseData = executeGraphQL(query, variables);
            
            // The data property is at the top level of the response
            return objectMapper.treeToValue(responseData, TournamentsResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch tournaments", e);
            throw new RuntimeException("Failed to fetch tournaments", e);
        }
    }
    
    public EventsResponse getEvents(Map<String, Object> variables) {
        log.debug("Fetching events with variables: {}", variables);
        
        // Ensure tournamentId is present
        if (!variables.containsKey("tournamentId")) {
            throw new IllegalArgumentException("tournamentId is required for events query");
        }
        
        try {
            String query = loadQueryFromFile("events/Events.graphql");
            log.debug("Using GraphQL query: {}", query);
            
            // Set the operation name to match the query
            variables.put("operationName", "EventsQuery");
            
            JsonNode responseData = executeGraphQL(query, variables);
            
            return objectMapper.treeToValue(responseData, EventsResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch events", e);
            throw new RuntimeException("Failed to fetch events", e);
        }
    }
    
    public SetsResponse getSets(Map<String, Object> variables) {
        log.debug("Fetching sets with variables: {}", variables);
        
        // Ensure eventId is present
        if (!variables.containsKey("eventId")) {
            throw new IllegalArgumentException("eventId is required for sets query");
        }
        
        try {
            String query = loadQueryFromFile("sets/Sets.graphql");
            log.debug("Using GraphQL query: {}", query);
            
            // Set the operation name to match the query
            variables.put("operationName", "SetsQuery");
            
            // Add default pagination if not provided
            if (!variables.containsKey("page")) {
                variables.put("page", 1);
            }
            if (!variables.containsKey("perPage")) {
                variables.put("perPage", 50);
            }
            
            JsonNode responseData = executeGraphQL(query, variables);
            
            return objectMapper.treeToValue(responseData, SetsResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch sets", e);
            throw new RuntimeException("Failed to fetch sets", e);
        }
    }
    
    public StandingsResponse getStandings(Map<String, Object> variables) {
        log.debug("Fetching standings with variables: {}", variables);
        
        // Ensure eventId is present
        if (!variables.containsKey("eventId")) {
            throw new IllegalArgumentException("eventId is required for standings query");
        }
        
        try {
            String query = loadQueryFromFile("standings/Standings.graphql");
            log.debug("Using GraphQL query: {}", query);
            
            // Set the operation name to match the query
            variables.put("operationName", "StandingsQuery");
            
            // Add default pagination if not provided
            if (!variables.containsKey("page")) {
                variables.put("page", 1);
            }
            if (!variables.containsKey("perPage")) {
                variables.put("perPage", 50);
            }
            
            JsonNode responseData = executeGraphQL(query, variables);
            
            return objectMapper.treeToValue(responseData, StandingsResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch standings", e);
            throw new RuntimeException("Failed to fetch standings", e);
        }
    }
    
    private JsonNode executeGraphQL(String query, Map<String, Object> variables) throws IOException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("query", query);
        
        String operationName = null;
        if (variables != null) {
            if (variables.containsKey("operationName")) {
                operationName = (String) variables.remove("operationName");
                requestBody.put("operationName", operationName);
            }
            
            if (!variables.isEmpty()) {
                requestBody.set("variables", objectMapper.valueToTree(variables));
            }
        }
        
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        log.debug("Request body: {}", requestBodyJson);
        
        RequestBody body = RequestBody.create(requestBodyJson, JSON);
        
        String token = graphQLConfig.getToken();
        if (token == null || token.isEmpty()) {
            throw new IOException("GraphQL token is not configured");
        }
        
        if (token.equals("your_api_token_here")) {
            log.error("You're using the placeholder token value. Please set a real Start.gg API token");
            throw new IOException("Invalid API token: using placeholder value");
        }
        
        // Mask token for logging
        String maskedToken = token.length() > 8 
            ? token.substring(0, 4) + "..." + token.substring(token.length() - 4) 
            : "****";
        log.debug("Using token: {}", maskedToken);
        
        Request request = new Request.Builder()
            .url(API_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer " + token)
            .build();
            
        log.debug("Sending request to {} with operation: {}", API_URL, operationName);
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            log.debug("Response body: {}", responseBody);
            
            if (!response.isSuccessful()) {
                log.error("GraphQL request failed with status code: {} and message: {}", 
                    response.code(), response.message());
                    
                if (response.code() == 400) {
                    log.error("A 400 error usually indicates an invalid token or malformed query");
                } else if (response.code() == 401) {
                    log.error("A 401 error indicates authentication failure. Check your API token");
                }
                
                throw new IOException("GraphQL request failed with status code: " + response.code());
            }
            
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            if (rootNode.has("errors")) {
                JsonNode errors = rootNode.get("errors");
                log.error("GraphQL errors: {}", errors);
                throw new IOException("GraphQL errors: " + errors.toString());
            }
            
            // Return the full response, including data and extensions
            return rootNode;
        }
    }
    
    private String loadQueryFromFile(String path) throws IOException {
        Path queryPath = Paths.get("src", "main", "graphql", path);
        return Files.readString(queryPath);
    }
} 
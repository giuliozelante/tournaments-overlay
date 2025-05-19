package it.tournaments.overlay.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
@Slf4j
public class GraphQLClient {
  private final OkHttpClient client;
  private final String graphqlEndpoint;
  private final String authToken;
  private final ObjectMapper objectMapper;

  public GraphQLClient(GraphQLConfig config, ObjectMapper objectMapper) {
    this.graphqlEndpoint = config.getEndpoint();
    this.authToken = config.getToken();
    this.objectMapper = objectMapper;

    this.client =
        new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
  }

  public JsonNode execute(String query, Map<String, Object> variables, String operationName)
      throws IOException {
    ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put("query", query);

    if (variables != null && !variables.isEmpty()) {
      requestBody.set("variables", objectMapper.valueToTree(variables));
    }

    if (operationName != null && !operationName.isEmpty()) {
      requestBody.put("operationName", operationName);
    }

    RequestBody body =
        RequestBody.create(
            objectMapper.writeValueAsString(requestBody), MediaType.parse("application/json"));

    Request.Builder requestBuilder = new Request.Builder().url(graphqlEndpoint).post(body);

    // Add authorization header if token is available
    if (authToken != null && !authToken.isEmpty()) {
      requestBuilder.addHeader("Authorization", "Bearer " + authToken);
    }

    Request request = requestBuilder.build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        log.error("GraphQL request failed with status code: {}", response.code());
        throw new IOException("GraphQL request failed with status code: " + response.code());
      }

      String responseBody = response.body().string();
      JsonNode rootNode = objectMapper.readTree(responseBody);

      if (rootNode.has("errors")) {
        JsonNode errors = rootNode.get("errors");
        log.error("GraphQL errors: {}", errors);
        throw new IOException("GraphQL errors: " + errors.toString());
      }

      return rootNode.get("data");
    }
  }
}

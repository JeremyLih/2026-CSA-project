package org.andromedax.csa.geminiapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiService(
            ObjectMapper objectMapper,
            @Value("${app.gemini.api-key}") String apiKey,
            @Value("${app.gemini.model}") String model
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateReply(String message) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Missing GEMINI_API_KEY environment variable."
            );
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + urlEncode(model)
                + ":generateContent";

        String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": %s }
                      ]
                    }
                  ]
                }
                """.formatted(toJsonString(message));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String upstreamMessage = extractErrorMessage(response.body());
                logger.warn("Gemini API error {}: {}", response.statusCode(), upstreamMessage);
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Gemini API returned HTTP " + response.statusCode() + ": " + upstreamMessage
                );
            }

            String reply = extractReply(response.body());
            if (reply == null || reply.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Gemini response did not include reply text."
                );
            }

            return reply;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini network error.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Gemini request interrupted.", e);
        }
    }

    private String extractReply(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            return textNode.isTextual() ? textNode.asText() : null;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to parse Gemini response.", e);
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageNode = root.path("error").path("message");
            if (messageNode.isTextual() && !messageNode.asText().isBlank()) {
                return messageNode.asText();
            }
        } catch (IOException ignored) {
            // Fall through to raw-body fallback.
        }

        if (responseBody == null || responseBody.isBlank()) {
            return "Empty error body from Gemini.";
        }

        return responseBody.length() > 400
                ? responseBody.substring(0, 400) + "..."
                : responseBody;
    }

    private String toJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize Gemini request.", e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

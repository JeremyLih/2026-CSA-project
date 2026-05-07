package org.andromedax.csa.geminiapi.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

@Repository
public class QuestionRepository {

    private final String url;
    private final String user;
    private final String password;
    private final ObjectMapper objectMapper;

    public QuestionRepository(
            @Value("${app.database.url}") String url,
            @Value("${app.database.user}") String user,
            @Value("${app.database.password}") String password,
            ObjectMapper objectMapper
    ) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.objectMapper = objectMapper;
    }

    public void insertQuestion(Map<String, Object> question) {
        if (user == null || user.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("Database credentials are not configured.");
        }

        String sql = """
                INSERT INTO questions (topic, difficulty, question, answersjson, correct_answer)
                VALUES (?, ?, ?, ?::jsonb, ?)
                """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, textValue(question.get("topic"), "AP Computer Science A"));
            stmt.setLong(2, longValue(question.get("difficulty"), 2));
            stmt.setString(3, textValue(question.get("text"), ""));
            stmt.setString(4, choicesJson(question));
            stmt.setString(5, textValue(question.get("correctChoice"), ""));

            stmt.executeUpdate();
        } catch (SQLException | JsonProcessingException exception) {
            throw new IllegalStateException("Failed to insert generated question.", exception);
        }
    }

    private String choicesJson(Map<String, Object> question) throws JsonProcessingException {
        return objectMapper.writeValueAsString(question.get("choices"));
    }

    private String textValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isBlank() ? fallback : text;
    }

    private long longValue(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}

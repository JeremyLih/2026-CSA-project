package org.andromedax.csa.geminiapi.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@CrossOrigin(origins = {
        "http://localhost:4173",
        "https://2026-csa-project.pages.dev"
})
@RestController
@RequestMapping("/api")
public class TestController {

    // ─────────────────────────────
    // START SESSION
    // ─────────────────────────────
    @PostMapping("/start-session")
    public Map<String, Object> startSession(@RequestBody Map<String, Object> body) {

        String studentId = (String) body.getOrDefault("studentId", "UNKNOWN");
        String testId = (String) body.getOrDefault("testId", "DEFAULT");

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", "SESSION-" + UUID.randomUUID());
        response.put("studentId", studentId);
        response.put("testId", testId);
        response.put("status", "started");

        return response;
    }

    // ─────────────────────────────
    // NEXT QUESTION
    // ─────────────────────────────
    @PostMapping("/next-question")
    public Map<String, Object> nextQuestion(@RequestBody Map<String, Object> body) {

        int difficulty = 3;

        if (body.get("difficulty") != null) {
            try {
                difficulty = ((Number) body.get("difficulty")).intValue();
            } catch (Exception ignored) {
                difficulty = 3;
            }
        }

        Map<String, Object> q = new HashMap<>();
        q.put("questionId", "Q-" + UUID.randomUUID());
        q.put("text", "What is " + difficulty + " + " + difficulty + "?");
        q.put("subject", "math");
        q.put("difficulty", difficulty);

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(Map.of("id", "A", "text", String.valueOf(difficulty)));
        choices.add(Map.of("id", "B", "text", String.valueOf(difficulty * 2)));
        choices.add(Map.of("id", "C", "text", String.valueOf(difficulty + 5)));
        choices.add(Map.of("id", "D", "text", String.valueOf(difficulty - 1)));

        q.put("choices", choices);
        q.put("correctChoice", "B");

        return q;
    }

    // ─────────────────────────────
    // SUBMIT TEST
    // ─────────────────────────────
    @PostMapping("/results/submit")
    public Map<String, Object> submitTest(@RequestBody Map<String, Object> body) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Test submitted successfully");
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    // ─────────────────────────────
    // HEALTH CHECK
    // ─────────────────────────────
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
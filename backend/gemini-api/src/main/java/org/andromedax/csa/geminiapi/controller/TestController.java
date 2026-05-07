package org.andromedax.csa.geminiapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.andromedax.csa.geminiapi.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@CrossOrigin(origins = {
        "http://localhost:4173",
        "https://2026-csa-project.pages.dev"
})
@RestController
@RequestMapping("/api")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public TestController(GeminiService geminiService, ObjectMapper objectMapper) {
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

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
        int difficulty = readDifficulty(body);

        try {
            String raw = geminiService.generateReply(buildQuestionPrompt(difficulty));
            Map<String, Object> question = parseQuestion(raw, difficulty);
            question.put("source", "gemini");
            question.put("model", geminiService.model());
            return question;
        } catch (Exception exception) {
            logger.warn("Gemini next-question generation failed; returning local fallback.", exception);
            Map<String, Object> fallback = fallbackQuestion(difficulty);
            fallback.put("source", "fallback");
            fallback.put("model", geminiService.model());
            fallback.put("warning", "Gemini generation unavailable; served local fallback question.");
            return fallback;
        }
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

    private int readDifficulty(Map<String, Object> body) {
        Object raw = body.get("difficulty");
        if (raw instanceof Number number) {
            return clampDifficulty(number.intValue());
        }
        if (raw instanceof String text) {
            try {
                return clampDifficulty(Integer.parseInt(text));
            } catch (NumberFormatException ignored) {
                return 2;
            }
        }
        return 2;
    }

    private int clampDifficulty(int difficulty) {
        return Math.max(1, Math.min(3, difficulty));
    }

    private String buildQuestionPrompt(int difficulty) {
        String label = switch (difficulty) {
            case 1 -> "easy";
            case 3 -> "hard";
            default -> "medium";
        };

        return """
                Generate one original AP Computer Science A multiple-choice question.
                Topic must be Java programming, arrays, ArrayList, classes, inheritance, recursion, loops, conditionals, algorithms, or Big-O.
                Difficulty: %s (%d/3).
                Do not generate arithmetic-only questions.

                Return only valid JSON, with no markdown and no explanation:
                {
                  "questionId": "Q-short-random-id",
                  "subject": "AP Computer Science A",
                  "difficulty": %d,
                  "text": "Question text",
                  "choices": [
                    {"id": "A", "text": "Choice A"},
                    {"id": "B", "text": "Choice B"},
                    {"id": "C", "text": "Choice C"},
                    {"id": "D", "text": "Choice D"}
                  ],
                  "correctChoice": "A"
                }
                """.formatted(label, difficulty, difficulty);
    }

    private Map<String, Object> parseQuestion(String raw, int difficulty) {
        try {
            String json = stripJson(raw);
            Map<String, Object> parsed = objectMapper.readValue(json, MAP_TYPE);
            return normalizeQuestion(parsed, difficulty);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Gemini returned invalid question JSON.", exception);
        }
    }

    private String stripJson(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("No JSON object in Gemini response.");
        }
        return text.substring(start, end + 1);
    }

    private Map<String, Object> normalizeQuestion(Map<String, Object> parsed, int difficulty) {
        Object choicesRaw = parsed.get("choices");
        if (!(choicesRaw instanceof List<?> choices) || choices.size() != 4) {
            throw new IllegalArgumentException("Question must contain exactly four choices.");
        }

        List<Map<String, String>> normalizedChoices = new ArrayList<>();
        for (int i = 0; i < choices.size(); i++) {
            Object choiceRaw = choices.get(i);
            if (!(choiceRaw instanceof Map<?, ?> choice)) {
                throw new IllegalArgumentException("Choice must be an object.");
            }
            Object idRaw = choice.get("id");
            Object textRaw = choice.get("text");
            String id = idRaw == null ? String.valueOf((char) ('A' + i)) : String.valueOf(idRaw);
            String text = textRaw == null ? "" : String.valueOf(textRaw).trim();
            if (text.isBlank()) {
                throw new IllegalArgumentException("Choice text must not be blank.");
            }
            normalizedChoices.add(Map.of("id", id, "text", text));
        }

        String correctChoice = String.valueOf(parsed.getOrDefault("correctChoice", "")).trim();
        if (correctChoice.isBlank()) {
            correctChoice = String.valueOf(parsed.getOrDefault("correct_answer", "")).trim();
        }
        if (!List.of("A", "B", "C", "D").contains(correctChoice)) {
            throw new IllegalArgumentException("correctChoice must be A, B, C, or D.");
        }

        String text = String.valueOf(parsed.getOrDefault("text", parsed.getOrDefault("question", ""))).trim();
        if (text.isBlank()) {
            throw new IllegalArgumentException("Question text must not be blank.");
        }

        Map<String, Object> question = new LinkedHashMap<>();
        question.put("questionId", String.valueOf(parsed.getOrDefault("questionId", "Q-" + UUID.randomUUID())));
        question.put("subject", String.valueOf(parsed.getOrDefault("subject", "AP Computer Science A")));
        question.put("difficulty", difficulty);
        question.put("text", text);
        question.put("choices", normalizedChoices);
        question.put("correctChoice", correctChoice);
        return question;
    }

    private Map<String, Object> fallbackQuestion(int difficulty) {
        List<Map<String, Object>> bank = List.of(
                Map.of(
                        "text", "What is printed by the following Java code? int[] a = {2, 4, 6}; System.out.print(a[1]);",
                        "choices", List.of(
                                Map.of("id", "A", "text", "2"),
                                Map.of("id", "B", "text", "4"),
                                Map.of("id", "C", "text", "6"),
                                Map.of("id", "D", "text", "An ArrayIndexOutOfBoundsException")
                        ),
                        "correctChoice", "B"
                ),
                Map.of(
                        "text", "Which Java loop header correctly visits every valid index of an array named values?",
                        "choices", List.of(
                                Map.of("id", "A", "text", "for (int i = 0; i <= values.length; i++)"),
                                Map.of("id", "B", "text", "for (int i = 1; i < values.length; i++)"),
                                Map.of("id", "C", "text", "for (int i = 0; i < values.length; i++)"),
                                Map.of("id", "D", "text", "for (int i = 0; i < values.length - 1; i++)")
                        ),
                        "correctChoice", "C"
                ),
                Map.of(
                        "text", "A method recursively halves the input size each call and does constant work per call. What is its time complexity?",
                        "choices", List.of(
                                Map.of("id", "A", "text", "O(1)"),
                                Map.of("id", "B", "text", "O(log n)"),
                                Map.of("id", "C", "text", "O(n)"),
                                Map.of("id", "D", "text", "O(n log n)")
                        ),
                        "correctChoice", "B"
                )
        );

        int index = Math.floorMod(Objects.hash(System.nanoTime(), difficulty), bank.size());
        Map<String, Object> selected = bank.get(index);
        Map<String, Object> question = new LinkedHashMap<>();
        question.put("questionId", "Q-FALLBACK-" + UUID.randomUUID());
        question.put("subject", "AP Computer Science A");
        question.put("difficulty", difficulty);
        question.put("text", selected.get("text"));
        question.put("choices", selected.get("choices"));
        question.put("correctChoice", selected.get("correctChoice"));
        return question;
    }

}

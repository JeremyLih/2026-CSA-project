package org.andromedax.csa.geminiapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.andromedax.csa.geminiapi.repository.QuestionRepository;
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
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public TestController(GeminiService geminiService, QuestionRepository questionRepository, ObjectMapper objectMapper) {
        this.geminiService = geminiService;
        this.questionRepository = questionRepository;
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
        int unit = readUnit(body);

        try {
            String raw = geminiService.generateReply(buildQuestionPrompt(getUnitContext(unit), difficulty));
            Map<String, Object> question = parseQuestion(raw, difficulty);
            question.put("source", "gemini");
            question.put("model", geminiService.model());
            question.put("unit", unit);
            question.put("databaseSaved", saveGeneratedQuestion(question));
            return question;
        } catch (Exception exception) {
            logger.warn("Gemini next-question generation failed; returning local fallback.", exception);
            Map<String, Object> fallback = fallbackQuestion(difficulty);
            fallback.put("source", "fallback");
            fallback.put("model", geminiService.model());
            fallback.put("unit", unit);
            fallback.put("warning", "Gemini generation unavailable; served local fallback question.");
            fallback.put("databaseSaved", saveGeneratedQuestion(fallback));
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

    private boolean saveGeneratedQuestion(Map<String, Object> question) {
        try {
            questionRepository.insertQuestion(question);
            return true;
        } catch (Exception exception) {
            logger.warn("Generated question was not saved to the database.", exception);
            return false;
        }
    }

    private int readUnit(Map<String, Object> body) {
        Object raw = body.get("unit");
        if (raw instanceof Number number) {
            return clampUnit(number.intValue());
        }
        if (raw instanceof String text) {
            try {
                return clampUnit(Integer.parseInt(text));
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
        return 1;
    }

    private int clampUnit(int unit) {
        return Math.max(1, Math.min(10, unit));
    }

    private String getUnitContext(int unit) {
        return switch (unit) {
            case 1 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 1
                    TITLE: Primitive Types

                    CONTENT:
                    Variables, assignment, primitive numeric types, arithmetic expressions, integer division,
                    casting, compound assignment, operator precedence, and common numeric edge cases.
                    """;
            case 2 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 2
                    TITLE: Using Objects

                    CONTENT:
                    Object creation, constructors, method calls, String methods, object references,
                    null references, wrapper classes, and using Java library classes.
                    """;
            case 3 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 3
                    TITLE: Boolean Expressions and if Statements

                    CONTENT:
                    Boolean expressions, relational operators, logical operators, short-circuit evaluation,
                    if/else control flow, De Morgan's laws, and equivalent conditional logic.
                    """;
            case 4 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 4
                    TITLE: Iteration

                    CONTENT:
                    while loops, for loops, nested loops, loop bounds, off-by-one errors,
                    accumulators, counters, tracing loop state, and termination conditions.
                    """;
            case 5 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 5
                    TITLE: Writing Classes

                    CONTENT:
                    Instance variables, constructors, methods, parameters, return values, encapsulation,
                    access modifiers, static members, this, and object state changes.
                    """;
            case 6 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 6
                    TITLE: Array

                    CONTENT:
                    Array creation, indexing, traversal, enhanced for loops, bounds errors,
                    searching, counting, modifying elements, and array algorithm patterns.
                    """;
            case 7 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 7
                    TITLE: ArrayList

                    CONTENT:
                    ArrayList methods, indexing, adding/removing elements, traversal while mutating,
                    size changes, object references in lists, and common skip/shift bugs.
                    """;
            case 8 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 8
                    TITLE: 2D Array

                    CONTENT:
                    Row-major traversal, nested loops, row and column indices, rectangular arrays,
                    matrix algorithms, and index-bound reasoning.
                    """;
            case 9 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 9
                    TITLE: Inheritance

                    CONTENT:
                    Superclasses, subclasses, overriding, overloading, super calls, polymorphism,
                    dynamic dispatch, constructor chaining, and reference vs object type.
                    """;
            case 10 -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: 10
                    TITLE: Recursion

                    CONTENT:
                    Recursive methods, base cases, recursive calls, call stacks, return values,
                    recursive tracing, and common termination mistakes.
                    """;
            default -> """
                    AP CSA CURRICULUM CONTEXT

                    UNIT: General
                    TITLE: General AP Computer Science A

                    CONTENT:
                    Standard AP Computer Science A topics including primitive types, objects,
                    conditionals, iteration, classes, arrays, ArrayList, 2D arrays, inheritance, and recursion.
                    """;
        };
    }

    private String buildQuestionPrompt(String unitContext, int difficulty) {
        return """
               TASK: JSON_OUTPUT_GENERATION

               MODE: STRICT

               CURRICULUM CONTEXT:
                %s

               HARD RULE:
                - Only generate questions based on the provided unit context
                - Do NOT introduce outside topics
                - Stay strictly within AP CSA material in the context

               QUESTION STYLE RULES:
                - DO NOT overuse "What is the output of this code?"-style questions
                - AT MOST 25 percent of generated questions may be output-prediction style
                - Prefer conceptual reasoning, logic tracing, and design-based questions
                - Preferred questions involve:
                  - algorithm reasoning
                  - edge cases
                  - debugging incorrect code
                  - comparing multiple implementations
                  - time/space reasoning (conceptual, not Big-O heavy)
                  - object-oriented behavior and interactions
                - The question must test understanding of a specific concept from the unit
                - The concept being tested should be inferable but not explicitly stated anywhere

               QUESTION VARIETY REQUIREMENT:
                The question MUST be one of the following types:
                - Conceptual reasoning (no code required)
                - Code comprehension with reasoning (NOT just output)
                - Debugging / error identification
                - Code completion / missing logic
                - Object interaction / method behavior
                - Scenario-based problem solving
                Reject simple recall or trivial output questions.

               ANSWER CHOICE RULES:
                - All incorrect choices must be plausible
                - Avoid obviously wrong answers
                - Distractors should reflect common student mistakes
                - For high difficulty, choices should be very similar, forcing students to think harder

               INTERNAL VALIDATION (DO NOT OUTPUT):
                - Ensure the question cannot be answered in under 10 seconds
                - Ensure at least one incorrect choice is a common misconception
                - Ensure the correct answer is unambiguous

               OUTPUT_RULES:
                - Output must be valid JSON
                - Output must contain no text outside JSON
                - Output must not include markdown, comments, or backticks
                - Output must begin with { and end with }

               SCHEMA:
                {
                  "topic": string,
                  "text": string,
                  "choices": [
                    {"id":"A","text":string},
                    {"id":"B","text":string},
                    {"id":"C","text":string},
                    {"id":"D","text":string}
                  ],
                  "correctChoice": "A" | "B" | "C" | "D"
                }

               DIFFICULTY RULES:

               difficulty = 1:
                - single concept
                - no traps
                - direct recall or simple application

               difficulty = 2:
                - 1-2 concepts
                - minimal reasoning
                - plausible distractors based on common mistakes

               difficulty = 3:
                - multiple steps
                - requires careful reading
                - may include small traps, edge cases, or similar-looking choices
                - should not be solvable by quick surface inspection

               CREATE THE QUESTION BASED ON DIFFICULTY LEVEL %d.

               RETURN: JSON_ONLY
               """.formatted(unitContext, difficulty);
    }

    private Map<String, Object> parseQuestion(String raw, int difficulty) {
        try {
            String json = stripJson(raw);
            Map<String, Object> parsed = objectMapper.readValue(json, MAP_TYPE);
            return shuffleChoices(normalizeQuestion(parsed, difficulty));
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
        question.put("topic", String.valueOf(parsed.getOrDefault("topic", "AP Computer Science A")));
        question.put("subject", "AP Computer Science A");
        question.put("difficulty", difficulty);
        question.put("text", text);
        question.put("choices", normalizedChoices);
        question.put("correctChoice", correctChoice);
        return question;
    }

    private Map<String, Object> shuffleChoices(Map<String, Object> question) {
        Object choicesRaw = question.get("choices");
        if (!(choicesRaw instanceof List<?> choices)) {
            return question;
        }

        List<Map<String, String>> shuffled = new ArrayList<>();
        for (Object choiceRaw : choices) {
            if (choiceRaw instanceof Map<?, ?> choice) {
                shuffled.add(Map.of(
                        "id", String.valueOf(choice.get("id")),
                        "text", String.valueOf(choice.get("text"))
                ));
            }
        }

        Collections.shuffle(shuffled);
        String oldCorrect = String.valueOf(question.get("correctChoice"));
        String[] labels = {"A", "B", "C", "D"};
        List<Map<String, String>> relabeled = new ArrayList<>();
        String newCorrect = oldCorrect;

        for (int i = 0; i < shuffled.size(); i++) {
            Map<String, String> choice = shuffled.get(i);
            String newId = labels[i];
            if (choice.get("id").equals(oldCorrect)) {
                newCorrect = newId;
            }
            relabeled.add(Map.of("id", newId, "text", choice.get("text")));
        }

        question.put("choices", relabeled);
        question.put("correctChoice", newCorrect);
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
        question.put("topic", "AP Computer Science A");
        question.put("subject", "AP Computer Science A");
        question.put("difficulty", difficulty);
        question.put("text", selected.get("text"));
        question.put("choices", selected.get("choices"));
        question.put("correctChoice", selected.get("correctChoice"));
        return question;
    }

}

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class GeminiQuestion {
    String topic;
    int difficulty;
    String question;
    Map<String, String> answers;
    String correct_answer;

    public static GeneratedQuestion generate(Gemini gemini, int difficultyLevel) {
        return generate(gemini, difficultyLevel, null);
    }

    public static GeneratedQuestion generate(Gemini gemini, int difficultyLevel, Boolean previousAnswerCorrect) {
        int normalizedDifficulty = Math.max(1, Math.min(5, difficultyLevel));
        String difficultyLabel = switch (normalizedDifficulty) {
            case 1 -> "easy";
            case 2 -> "medium-easy";
            case 3 -> "medium";
            case 4 -> "hard";
            case 5 -> "very hard";
            default -> "medium";
        };
        String adaptiveInstruction = previousAnswerCorrect == null
                ? "This is the first question. Start easy."
                : previousAnswerCorrect
                    ? "The student answered the previous question correctly, so generate a harder next question."
                    : "The student answered the previous question incorrectly, so generate an easier next question.";

        String prompt = """
            Generate a multiple choice question about computer science algorithms.
            Adaptive instruction: %s
            Difficulty level: %d out of 5
            Difficulty label: %s

            Higher difficulty numbers must require more reasoning. Lower difficulty numbers must be simpler.

            Respond ONLY with valid JSON in this exact format, no extra text, no markdown:
            {
              "topic": "sorting algorithms",
              "difficulty": %d,
              "question": "Your question here?",
              "answers": {
                "A": "First choice",
                "B": "Second choice",
                "C": "Third choice",
                "D": "Fourth choice"
              },
              "correct_answer": "A"
            }
            """.formatted(adaptiveInstruction, normalizedDifficulty, difficultyLabel, normalizedDifficulty);

        String response = gemini.generateReply(prompt).trim();

        // Strip markdown code fences if Gemini wraps it
        if (response.startsWith("```")) {
            response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        }

        GeminiQuestion gq = new Gson().fromJson(response, GeminiQuestion.class);

        List<GeneratedQuestion.Choice> choiceList = new ArrayList<>();
        if (gq.answers != null) {
            gq.answers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> choiceList.add(new GeneratedQuestion.Choice(e.getKey(), e.getValue())));
        }

        return new GeneratedQuestion(
            UUID.randomUUID().toString(),
            gq.topic != null ? gq.topic : "general",
            gq.question,
            choiceList,
            gq.correct_answer,
            normalizedDifficulty
        );
    }
}

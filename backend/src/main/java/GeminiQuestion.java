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
        String difficultyLabel = switch (difficultyLevel) {
            case 1 -> "easy";
            case 3 -> "hard";
            default -> "medium";
        };

        String prompt = """
            Generate a multiple choice question about computer science algorithms.
            Difficulty: %s

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
            """.formatted(difficultyLabel, difficultyLevel);

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
            gq.difficulty
        );
    }
}
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class NextQuestionHandler implements HttpHandler {

    private final SessionStore sessionStore;
    private final Gemini gemini;

    public NextQuestionHandler(SessionStore sessionStore, Gemini gemini) {
        this.sessionStore = sessionStore;
        this.gemini = gemini;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpResponses.sendJson(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpResponses.sendJson(exchange, 405, HttpResponses.error("Method not allowed."));
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Object parsed = Json.parse(body);
            if (!(parsed instanceof Map<?, ?> payload)) {
                HttpResponses.sendJson(exchange, 400, HttpResponses.error("Invalid JSON body."));
                return;
            }

            String sessionId = (String) payload.get("sessionId");
            Object difficultyRaw = payload.get("difficulty");
            int difficulty = difficultyRaw instanceof Number n ? n.intValue() : 2;
            Object wasCorrectRaw = payload.get("wasCorrect");
            Boolean wasCorrect = wasCorrectRaw instanceof Boolean b ? b : null;

            if (sessionId == null || sessionId.isBlank()) {
                HttpResponses.sendJson(exchange, 400, HttpResponses.error("`sessionId` is required."));
                return;
            }

            // Just log the sessionId, don't block on it
            System.out.println("Question request for session: " + sessionId);

            GeneratedQuestion question = GeminiQuestion.generate(gemini, difficulty);

            String json = """
            {
              "questionId": "%s",
              "text": "%s",
              "choices": [%s],
              "correctChoice": "%s",
              "difficulty": %d
            }
            """.formatted(
                question.questionId(),
                Json.escape(question.text()),
                formatChoices(question.choices()),
                question.correctChoice(),
                question.difficulty()
            );

            HttpResponses.sendJson(exchange, 200, json);

        } catch (Exception e) {
            e.printStackTrace();
            HttpResponses.sendJson(exchange, 500, HttpResponses.error("Server error: " + e.getMessage()));
        } finally {
            exchange.close();
        }
    }

    private String formatChoices(List<GeneratedQuestion.Choice> choices) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < choices.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("""
                {"id":"%s","text":"%s"}
            """.formatted(
                choices.get(i).id(),
                Json.escape(choices.get(i).text())
            ));
        }
        return sb.toString();
    }
}

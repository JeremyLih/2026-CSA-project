import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStore {

    private final Map<String, TestSession> sessions = new ConcurrentHashMap<>();

    public TestSession createSession(String studentId, String testId) {
        String sessionId = UUID.randomUUID().toString();
        SessionData data = new SessionData(
                1,
                1,
                null,
                Instant.now().toEpochMilli(),
                0,
                0,
                "active"
        );

        TestSession session = new TestSession(sessionId, studentId, testId, data);
        sessions.put(sessionId, session);
        return session;
    }

    public TestSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void saveSession(String sessionId, TestSession session) {
        sessions.put(sessionId, session);
    }
}

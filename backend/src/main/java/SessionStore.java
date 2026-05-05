import com.google.gson.Gson;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;

public class SessionStore {

    private static final Gson gson = new Gson();

    // ─────────────────────────────
    // CREATE SESSION
    // ─────────────────────────────
    public TestSession createSession(String studentId, String testId) {

        String sql = """
            INSERT INTO sessions (id, student_id, created_at, data)
            VALUES (?::uuid, ?, now(), ?::jsonb)
        """;

        String sessionId = UUID.randomUUID().toString();

        SessionData data = new SessionData(
                1,
                2,
                null,
                Instant.now().toEpochMilli(),
                0,
                0,
                "active"
        );

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);
            stmt.setString(2, studentId);

            // embed testId inside JSON since schema only has student_id + data
            String json = gson.toJson(new SessionWrapper(testId, data));

            stmt.setString(3, json);

            stmt.executeUpdate();

            return new TestSession(sessionId, studentId, testId, data);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create session", e);
        }
    }

    // ─────────────────────────────
    // GET SESSION
    // ─────────────────────────────
    public TestSession getSession(String sessionId) {

        String sql = """
            SELECT id, student_id, data
            FROM sessions
            WHERE id = ?::uuid
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return null;

            String studentId = rs.getString("student_id");
            String json = rs.getString("data");

            SessionWrapper wrapper = gson.fromJson(json, SessionWrapper.class);

            return new TestSession(
                    rs.getString("id"),
                    studentId,
                    wrapper.testId(),
                    wrapper.data()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to load session", e);
        }
    }

    // ─────────────────────────────
    // UPDATE SESSION
    // ─────────────────────────────
    public void saveSession(String sessionId, TestSession session) {

        String sql = """
            UPDATE sessions
            SET data = ?::jsonb
            WHERE id = ?::uuid
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            SessionWrapper wrapper = new SessionWrapper(
                    session.testId(),
                    session.data()
            );

            stmt.setString(1, gson.toJson(wrapper));
            stmt.setString(2, sessionId);

            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update session", e);
        }
    }

    // ─────────────────────────────
    // INTERNAL WRAPPER (IMPORTANT)
    // ─────────────────────────────
    private record SessionWrapper(String testId, SessionData data) {}
}
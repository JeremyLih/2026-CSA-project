import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class Database {

    private static final String URL =
            "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require";

    // ─────────────────────────────
    // CONNECTION POOL
    // ─────────────────────────────
    private static final HikariDataSource pool;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(requireEnv("DB_USER"));
        config.setPassword(requireEnv("DB_PASSWORD"));

        config.setMaximumPoolSize(10);     // max open connections
        config.setMinimumIdle(2);          // keep 2 warm at all times
        config.setConnectionTimeout(3000); // fail fast if pool exhausted (ms)
        config.setIdleTimeout(30000);      // close idle connections after 30s
        config.setMaxLifetime(600000);     // recycle connections every 10 min

        pool = new HikariDataSource(config);
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + key);
        }
        return value;
    }

    public static Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    // ─────────────────────────────
    // QUESTIONS
    // ─────────────────────────────km
    public static void insertQuestion(String topic, long difficulty, String question, String answersjson, String correct_answer) {

        String sql = """
                INSERT INTO questions (topic, difficulty, question, answersjson, correct_answer)
                VALUES (?, ?, ?, ?::jsonb, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, topic);
            stmt.setLong(2, difficulty);
            stmt.setString(3, question);
            stmt.setString(4, answersjson);
            stmt.setString(5, correct_answer);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────
    // STUDENTS
    // ─────────────────────────────
    public static long getOrCreateStudent(String name) {

        // Single round trip instead of SELECT then INSERT
        String sql = """
                INSERT INTO students (name) VALUES (?)
                ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                RETURNING id
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getLong("id");

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ─────────────────────────────
    // SESSIONS
    // ─────────────────────────────
    public static long createSession(long studentId) {

        String sql = """
                INSERT INTO sessions (student_id, current_difficulty, status)
                VALUES (?, 'easy', 'active')
                RETURNING id
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getLong("id");

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ─────────────────────────────
    // CHAT MESSAGES (needed for Gemini memory)
    // ─────────────────────────────
    public static void saveMessage(long sessionId, String role, String content) {

        String sql = """
                INSERT INTO messages (session_id, role, content)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);
            stmt.setString(2, role);
            stmt.setString(3, content);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getSessionHistory(long sessionId) {

        String sql = """
                SELECT role, content
                FROM messages
                WHERE session_id = ?
                ORDER BY id ASC
                """;

        StringBuilder history = new StringBuilder();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                history.append(rs.getString("role"))
                        .append(": ")
                        .append(rs.getString("content"))
                        .append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history.toString();
    }
}
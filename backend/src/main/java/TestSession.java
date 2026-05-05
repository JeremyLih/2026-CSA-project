public class TestSession {

    private final String sessionId;
    private final String studentId;
    private final String testId;
    private SessionData data;

    public TestSession(String sessionId, String studentId, String testId, SessionData data) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.testId = testId;
        this.data = data;
    }

    public String sessionId() { return sessionId; }
    public String studentId() { return studentId; }
    public String testId() { return testId; }

    public SessionData data() { return data; }

    public void setData(SessionData data) {
        this.data = data;
    }
}
import java.time.OffsetDateTime;
import java.util.UUID;

public class ClassMember {
    private UUID id;
    private UUID classId;
    private UUID userId;

    private String role; // "student" or "teacher"
    private OffsetDateTime joinedAt;

    // Getters + Setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getClassId() { return classId; }
    public void setClassId(UUID classId) { this.classId = classId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public OffsetDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(OffsetDateTime joinedAt) { this.joinedAt = joinedAt; }
}
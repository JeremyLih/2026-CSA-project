public record SessionData(
        int currentUnit,
        int currentDifficulty,
        GeneratedQuestion currentQuestion,
        long lastUpdated,
        int correctCount,
        int incorrectCount,
        String status
) {}
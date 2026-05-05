function resolveBackendBase() {
    const host = window.location.hostname;
    const isLocal = host === "localhost" || host === "127.0.0.1";
    return isLocal ? "http://localhost:8080" : "https://cs.andromedax.org";
}

function startSession() {
    const session = getSession();
    if (!session) return;

    if (sessionStorage.getItem("sessionStarted") === "true") return;
    sessionStorage.setItem("sessionStarted", "true");

    const testId = sessionStorage.getItem("activeTestId") || "TEST-001";

    fetch(`${resolveBackendBase()}/api/start-session`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            studentId: session.student.id,
            testId: testId
        })
    })
        .then(r => r.json())
        .then(data => {
            sessionStorage.setItem("currentSessionId", data.sessionId);
        });
}

(function init() {
    if (typeof requireSession === "function") {
        requireSession();
    }

    setTimeout(startSession, 200);
})();

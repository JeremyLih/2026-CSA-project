function resolveBackendBase() {
    const host = window.location.hostname;
    const isLocal = host === "localhost" || host === "127.0.0.1";
    return isLocal ? "http://localhost:8080" : "https://cs.andromedax.org";
}

const API_BASE_URL = resolveBackendBase();

//
// LOGIN
// TODO: backend has no /api/login endpoint yet; revisit when auth is wired up.
//
async function handleLoginSubmit(event) {
    event.preventDefault();

    const loginData = {
        studentId: document.getElementById('student-id').value,
        password: document.getElementById('password').value
    };

    const response = await fetch(`${API_BASE_URL}/api/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginData)
    });

    const data = await response.json();
    console.log("Login response:", data);

    // store session info (important for Next Question)
    sessionStorage.setItem("sessionId", data.sessionId);
}

//
// START TEST
//
async function startTest(testId) {

    const studentId = sessionStorage.getItem('studentId') || 'STU-DEMO';

    const response = await fetch(`${API_BASE_URL}/api/start-session`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ studentId, testId })
    });

    const data = await response.json();
    console.log("Start test:", data);

    sessionStorage.setItem("sessionId", data.sessionId);
}

//
// RESULTS
// TODO: backend has no /api/results/:id endpoint yet; results are still
// persisted client-side via sessionStorage in test.html.
//
async function showFinalScore() {

    const resultId = sessionStorage.getItem('lastResultId');

    const response = await fetch(`${API_BASE_URL}/api/results/${resultId}`);

    const scoreData = await response.json();

    console.log("Student Score is:", scoreData.score);
}

//
// NEXT QUESTION
//
async function getNextQuestion() {

    console.log("Next button clicked!");

    const sessionId = sessionStorage.getItem("sessionId");
    const difficulty = sessionStorage.getItem("difficulty") || 2;

    const testData = {
        sessionId: sessionId,
        difficulty: Number(difficulty)
    };

    const response = await fetch(`${API_BASE_URL}/api/next-question`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(testData)
    });

    const data = await response.json();

    console.log("Question received:", data);

    // UPDATE QUESTION TEXT
    document.getElementById("questionText").innerHTML = data.text;

    // UPDATE CHOICES 
    const list = document.getElementById("choices-list");
    list.innerHTML = "";

    data.choices.forEach(choice => {
        const li = document.createElement("li");

        li.innerHTML = `
            <button class="choice-btn"
                    onclick="selectAnswer('${data.questionId}', '${choice.id}', this)">
                <span class="choice-letter">${choice.id}</span>
                <span>${choice.text}</span>
            </button>
        `;

        list.appendChild(li);
    });

    // SAVE NEW DIFFICULTY
    sessionStorage.setItem("difficulty", data.difficulty);
}

// 
// SELECT ANSWER (needed for Next button flow)
// 
let selectedAnswer = null;

if (typeof window.selectAnswer !== "function") {
    window.selectAnswer = function selectAnswer(questionId, choiceId, element) {
        selectedAnswer = choiceId;

        // remove old selection
        document.querySelectorAll(".choice-btn").forEach(btn => {
            btn.classList.remove("selected");
        });

        // highlight selected
        element.classList.add("selected");
    };
}

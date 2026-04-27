/* ============================================================
         DEMO DATA + STATE
         ------------------------------------------------------------
         This whole script is front-end demo logic.
         It uses localStorage so your demo remembers courses/tests.
         Later, these functions can be replaced with real API calls.
         ============================================================ */

const STORAGE_KEY = "teacherDashboardDataV2";

let selectedCourseId = null;
let selectedTestId = null;
let draftQuestions = [];

/* Placeholder question bank separated by type. */
const questionBank = [
  {
    id: "MC-001",
    type: "Multiple Choice",
    topic: "Sorting",
    difficulty: "Medium",
    question: "What is the average-case time complexity of merge sort?",
    choices: {
      A: "O(n)",
      B: "O(n log n)",
      C: "O(n²)",
      D: "O(log n)",
    },
    correct: "B",
  },
  {
    id: "MC-002",
    type: "Multiple Choice",
    topic: "Arrays",
    difficulty: "Easy",
    question: "Which index is the first element of an array stored at in Java?",
    choices: {
      A: "0",
      B: "1",
      C: "-1",
      D: "array.length",
    },
    correct: "A",
  },
  {
    id: "FRQ-001",
    type: "Free Response",
    topic: "ArrayLists",
    difficulty: "Medium",
    question:
      "Write a method that removes all even integers from an ArrayList<Integer>.",
    correct: "Teacher rubric placeholder",
  },
  {
    id: "FRQ-002",
    type: "Free Response",
    topic: "OOP",
    difficulty: "Hard",
    question:
      "Design a class hierarchy for different types of assessments in a testing platform.",
    correct: "Teacher rubric placeholder",
  },
];

/* Placeholder completed results. */
const completedTests = [
  {
    id: "RES-001",
    student: "Jane Student",
    test: "Sorting Algorithms Quiz",
    courseId: "COURSE-001",
    score: "85%",
    date: "Apr 18, 2026",
    status: "Passed",
  },
  {
    id: "RES-002",
    student: "Maya Patel",
    test: "ArrayList Review",
    courseId: "COURSE-001",
    score: "76%",
    date: "Apr 19, 2026",
    status: "Completed",
  },
  {
    id: "RES-003",
    student: "Alex Chen",
    test: "Arrays and ArrayLists",
    courseId: "COURSE-002",
    score: "72%",
    date: "Apr 17, 2026",
    status: "Completed",
  },
];

let teacherData = loadData();

function loadData() {
  const saved = localStorage.getItem(STORAGE_KEY);

  if (saved) {
    return JSON.parse(saved);
  }

  return {
    teacher: null,

    courses: [
      {
        id: "COURSE-001",
        name: "AP CSA Block B",
        code: "CSA-B-2026",
        unit: "Unit 7 — ArrayLists",
        pacing: "On Track",
        description:
          "Students are working on ArrayLists, traversal patterns, and common AP-style algorithms.",
      },
      {
        id: "COURSE-002",
        name: "AP CSA Block A",
        code: "CSA-A-2026",
        unit: "Unit 6 — Arrays",
        pacing: "Needs Review",
        description:
          "Students are reviewing arrays, indexing errors, and basic sorting algorithm traces.",
      },
    ],

    students: [
      {
        id: "STU-001",
        name: "Jane Student",
        email: "jane@example.com",
        courseId: "COURSE-001",
        status: "Confirmed",
      },
      {
        id: "STU-002",
        name: "Maya Patel",
        email: "maya@example.com",
        courseId: "COURSE-001",
        status: "Confirmed",
      },
      {
        id: "STU-003",
        name: "Alex Chen",
        email: "alex@example.com",
        courseId: "COURSE-002",
        status: "Confirmed",
      },
    ],

    joinRequests: [
      {
        id: "REQ-001",
        name: "Jordan Lee",
        courseId: "COURSE-001",
      },
    ],

    tests: [
      {
        id: "TEST-001",
        title: "Sorting Algorithms Quiz",
        courseId: "COURSE-001",
        type: "Test",
        timeLimit: "30",
        difficulty: "Medium",
        description:
          "Covers selection sort, insertion sort, merge sort, and algorithm tracing.",
        questions: [questionBank[0], questionBank[2]],
      },
      {
        id: "TEST-002",
        title: "Arrays and ArrayLists",
        courseId: "COURSE-002",
        type: "Assignment",
        timeLimit: "25",
        difficulty: "Easy",
        description:
          "Checks Java array indexing, traversal, and ArrayList methods.",
        questions: [questionBank[1]],
      },
    ],
  };
}

function saveData() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(teacherData));
}

/* ============================================================
         AUTH / ACCESS CONTROL
         ------------------------------------------------------------
         The dashboard wrapper is hidden until teacherData.teacher exists.
         This prevents teachers from seeing creation/results tools before
         signing in.
         ============================================================ */

function signInWithGoogle() {
  teacherData.teacher = {
    name: "Demo Teacher",
    email: "teacher@example.com",
  };

  saveData();
  renderApp();
  showDashboard();
}

function signOutTeacher() {
  teacherData.teacher = null;
  selectedCourseId = null;
  selectedTestId = null;
  draftQuestions = [];

  saveData();
  renderApp();
}

function renderApp() {
  const isSignedIn = Boolean(teacherData.teacher);

  document.getElementById("login-screen").hidden = isSignedIn;
  document.getElementById("dashboard-screen").hidden = !isSignedIn;
  document.getElementById("teacher-badge").hidden = !isSignedIn;
  document.getElementById("teacher-nav").hidden = !isSignedIn;

  if (!isSignedIn) {
    return;
  }

  document.getElementById("teacher-name").textContent =
    teacherData.teacher.name;
  document.getElementById("teacher-avatar").textContent = getInitials(
    teacherData.teacher.name,
  );

  renderDashboard();
  renderSelectedCourse();
  renderDraftQuestions();
  renderQuestionBank();
}

function getInitials(name) {
  return name
    .split(" ")
    .map((part) => part[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

/* ============================================================
         SCREEN NAVIGATION
         ============================================================ */

function showScreen(screenName) {
  document.querySelectorAll(".app-screen").forEach((screen) => {
    screen.classList.remove("active-screen");
  });

  document
    .getElementById(`screen-${screenName}`)
    .classList.add("active-screen");

  document.querySelectorAll("[data-screen-button]").forEach((button) => {
    button.classList.remove("active");
  });

  const activeButton = document.querySelector(
    `[data-screen-button="${screenName}"]`,
  );
  if (activeButton) {
    activeButton.classList.add("active");
  }

  window.scrollTo({ top: 0, behavior: "smooth" });
}

function showDashboard() {
  selectedCourseId = null;
  selectedTestId = null;
  showScreen("dashboard");
  renderDashboard();
}

function openCourse(courseId) {
  selectedCourseId = courseId;
  selectedTestId = null;
  showScreen("course-detail");
  showCourseTab("overview");
  renderSelectedCourse();
}

function returnToCurrentCourse() {
  showScreen("course-detail");
  showCourseTab("tests");
}

function showCourseTabFromTest(tabName) {
  showScreen("course-detail");
  showCourseTab(tabName);
}

function showTestDetail(testId) {
  selectedTestId = testId;

  const test = teacherData.tests.find((test) => test.id === testId);
  document.getElementById("test-detail-title").textContent = test
    ? test.title
    : "TEST PAGE";

  showScreen("test-detail");
}

function showCourseTab(tabName) {
  document.querySelectorAll(".course-tab-screen").forEach((screen) => {
    screen.classList.remove("active-course-tab");
  });

  document.querySelectorAll(".course-tab").forEach((tab) => {
    tab.classList.remove("active");
  });

  document
    .getElementById(`course-tab-${tabName}`)
    .classList.add("active-course-tab");

  const activeTab = document.querySelector(`[data-course-tab="${tabName}"]`);
  if (activeTab) {
    activeTab.classList.add("active");
  }

  renderSelectedCourse();
}

/* ============================================================
         DASHBOARD RENDERING
         ============================================================ */

function renderDashboard() {
  const courseCount = teacherData.courses.length;
  const studentCount = teacherData.students.length;
  const testCount = teacherData.tests.length;

  document.getElementById("course-count").textContent = courseCount;
  document.getElementById("student-count").textContent = studentCount;
  document.getElementById("test-count").textContent = testCount;
  document.getElementById("completed-count").textContent =
    completedTests.length;

  const courseList = document.getElementById("dashboard-course-list");

  if (teacherData.courses.length === 0) {
    courseList.innerHTML = `
            <div class="empty-state">
              <h3>No courses yet</h3>
              <p>Create your first course to begin adding students and tests.</p>
            </div>
          `;
    return;
  }

  courseList.innerHTML = teacherData.courses
    .map((course) => {
      const students = getStudentsForCourse(course.id);
      const tests = getTestsForCourse(course.id);
      const results = getResultsForCourse(course.id);

      return `
            <article class="course-card clickable-card" onclick="openCourse('${course.id}')">
              <div class="course-card-top">
                <span class="badge badge-navy">${course.code}</span>
                <span class="badge badge-slate">${course.pacing}</span>
              </div>

              <h3>${course.name}</h3>
              <p>${course.description}</p>

              <div class="unit-pill">
                Current Unit: ${course.unit}
              </div>

              <div class="course-meta">
                <span>${students.length} student(s)</span>
                <span>${tests.length} test(s)</span>
                <span>${results.length} result(s)</span>
              </div>

              <button class="btn btn-sm btn-primary card-action" type="button">
                Open Course →
              </button>
            </article>
          `;
    })
    .join("");
}

function openCreateCoursePanel() {
  document.getElementById("create-course-panel").hidden = false;
  document
    .getElementById("create-course-panel")
    .scrollIntoView({ behavior: "smooth" });
}

function closeCreateCoursePanel() {
  document.getElementById("create-course-panel").hidden = true;
}

/* ============================================================
         COURSE RENDERING
         ============================================================ */

function renderSelectedCourse() {
  if (!selectedCourseId) {
    return;
  }

  const course = getSelectedCourse();
  if (!course) {
    return;
  }

  const students = getStudentsForCourse(course.id);
  const tests = getTestsForCourse(course.id);
  const results = getResultsForCourse(course.id);

  document.getElementById("course-detail-code").textContent = course.code;
  document.getElementById("course-detail-name").textContent = course.name;
  document.getElementById("course-detail-description").textContent =
    course.description;
  document.getElementById("course-detail-students").textContent =
    students.length;
  document.getElementById("course-detail-tests").textContent = tests.length;
  document.getElementById("course-detail-unit").textContent = course.unit;
  document.getElementById("course-join-code").textContent = course.code;

  renderCourseOverview(course, students, tests, results);
  renderCourseStudents(students);
  renderJoinRequests();
  renderCourseTests(tests);
  renderCourseResults(results);
}

function renderCourseOverview(course, students, tests, results) {
  const overview = document.getElementById("course-overview-content");

  overview.innerHTML = `
          <div class="preview-item">
            <strong>Current Unit</strong>
            <span>${course.unit}</span>
          </div>

          <div class="preview-item">
            <strong>Pacing</strong>
            <span>${course.pacing}</span>
          </div>

          <div class="preview-item">
            <strong>Class Size</strong>
            <span>${students.length} students enrolled</span>
          </div>

          <div class="preview-item">
            <strong>Assessments</strong>
            <span>${tests.length} created, ${results.length} completed results</span>
          </div>
        `;
}

function renderCourseStudents(students) {
  const studentList = document.getElementById("course-student-list");

  if (students.length === 0) {
    studentList.innerHTML = `
            <div class="empty-state">
              <h3>No students yet</h3>
              <p>Add students by email or approve join code requests.</p>
            </div>
          `;
    return;
  }

  studentList.innerHTML = students
    .map((student) => {
      return `
            <article class="student-card">
              <div>
                <h3>${student.name}</h3>
                <p>${student.email}</p>
              </div>

              <span class="badge badge-success">${student.status}</span>
            </article>
          `;
    })
    .join("");
}

function renderJoinRequests() {
  const requestList = document.getElementById("join-request-list");
  const requests = teacherData.joinRequests.filter(
    (request) => request.courseId === selectedCourseId,
  );

  if (requests.length === 0) {
    requestList.innerHTML = `
            <div class="empty-state compact">
              <p>No pending join requests.</p>
            </div>
          `;
    return;
  }

  requestList.innerHTML = requests
    .map((request) => {
      return `
            <div class="request-item">
              <div>
                <strong>${request.name}</strong>
                <p>Requested to join this course.</p>
              </div>

              <button class="btn btn-sm btn-primary" onclick="acceptJoinRequest('${request.id}')">
                Accept
              </button>
            </div>
          `;
    })
    .join("");
}

function renderCourseTests(tests) {
  const tableBody = document.getElementById("course-test-table-body");

  if (tests.length === 0) {
    tableBody.innerHTML = `
            <tr>
              <td colspan="7" class="empty-table">No tests or assignments created yet.</td>
            </tr>
          `;
    return;
  }

  tableBody.innerHTML = tests
    .map((test) => {
      return `
            <tr>
              <td>${test.title}</td>
              <td class="secondary">${test.type}</td>
              <td>${test.questions.length}</td>
              <td class="secondary">${test.timeLimit} min</td>
              <td><span class="badge badge-slate">${test.difficulty}</span></td>
              <td><span class="badge badge-success">Created</span></td>
              <td>
                <button class="btn btn-sm btn-primary" onclick="showTestDetail('${test.id}')">
                  Open
                </button>
              </td>
            </tr>
          `;
    })
    .join("");
}

function renderCourseResults(results) {
  const tableBody = document.getElementById("course-results-body");

  if (results.length === 0) {
    tableBody.innerHTML = `
            <tr>
              <td colspan="5" class="empty-table">No completed tests yet.</td>
            </tr>
          `;
    return;
  }

  tableBody.innerHTML = results
    .map((result) => {
      return `
            <tr>
              <td>${result.student}</td>
              <td>${result.test}</td>
              <td><strong>${result.score}</strong></td>
              <td class="secondary">${result.date}</td>
              <td><span class="badge badge-success">${result.status}</span></td>
            </tr>
          `;
    })
    .join("");
}

/* ============================================================
         QUESTION BANK + TEST DRAFT RENDERING
         ============================================================ */

function renderDraftQuestions() {
  const list = document.getElementById("draft-question-list");

  if (!list) {
    return;
  }

  if (draftQuestions.length === 0) {
    list.innerHTML = `
            <div class="empty-state compact">
              <p>No questions added yet. Open the question bank to add MCQs, FRQs, or AI-generated placeholders.</p>
            </div>
          `;
    return;
  }

  list.innerHTML = draftQuestions
    .map((question, index) => {
      return `
            <div class="question-item">
              <div>
                <strong>${index + 1}. ${question.question}</strong>
                <p>${question.type} · ${question.topic || "AI Generated"} · Correct/Rubric: ${question.correct}</p>
              </div>

              <button class="btn btn-sm btn-danger" type="button" onclick="removeDraftQuestion(${index})">
                Remove
              </button>
            </div>
          `;
    })
    .join("");
}

function renderQuestionBank() {
  const mcList = document.getElementById("mc-question-bank-list");
  const frqList = document.getElementById("frq-question-bank-list");

  if (!mcList || !frqList) {
    return;
  }

  const multipleChoiceQuestions = questionBank.filter(
    (question) => question.type === "Multiple Choice",
  );
  const freeResponseQuestions = questionBank.filter(
    (question) => question.type === "Free Response",
  );

  mcList.innerHTML = multipleChoiceQuestions
    .map((question) => renderBankQuestion(question))
    .join("");
  frqList.innerHTML = freeResponseQuestions
    .map((question) => renderBankQuestion(question))
    .join("");
}

function renderBankQuestion(question) {
  return `
          <div class="bank-question">
            <div>
              <div class="bank-meta">
                <span class="badge badge-navy">${question.topic}</span>
                <span class="badge badge-slate">${question.difficulty}</span>
              </div>

              <h3>${question.question}</h3>
              <p>${question.type}</p>
            </div>

            <button class="btn btn-sm btn-primary" type="button" onclick="addBankQuestion('${question.id}')">
              Add to Draft
            </button>
          </div>
        `;
}

function addBankQuestion(questionId) {
  const selectedQuestion = questionBank.find(
    (question) => question.id === questionId,
  );

  if (!selectedQuestion) {
    return;
  }

  draftQuestions.push({
    ...selectedQuestion,
    source: "bank",
  });

  renderDraftQuestions();
  showCourseTab("tests");
}

function generateAIQuestionPlaceholder() {
  const course = getSelectedCourse();

  const aiQuestion = {
    id: crypto.randomUUID(),
    type: "AI Generated Placeholder",
    topic: course ? course.unit : "Current Unit",
    difficulty: "Adaptive",
    question:
      "AI-generated question placeholder based on the selected course and student levels.",
    correct: "AI-generated answer/rubric placeholder",
    source: "ai",
  };

  draftQuestions.push(aiQuestion);
  renderDraftQuestions();
  showCourseTab("tests");
}

function removeDraftQuestion(index) {
  draftQuestions.splice(index, 1);
  renderDraftQuestions();
}

function clearDraftQuestions() {
  draftQuestions = [];
  renderDraftQuestions();
}

/* ============================================================
         FORM HANDLERS
         ============================================================ */

document.getElementById("course-form").addEventListener("submit", (event) => {
  event.preventDefault();

  const course = {
    id: crypto.randomUUID(),
    name: document.getElementById("course-name").value.trim(),
    code: document.getElementById("course-code").value.trim(),
    unit: document.getElementById("course-unit").value.trim(),
    pacing: document.getElementById("course-pacing").value,
    description: document.getElementById("course-description").value.trim(),
  };

  teacherData.courses.push(course);
  saveData();

  event.target.reset();
  closeCreateCoursePanel();
  renderApp();
});

document
  .getElementById("student-email-form")
  .addEventListener("submit", async (event) => {
    // Stop the page from refreshing when the form submits.
    event.preventDefault();

    // Safety check: the teacher must be inside a course page.
    if (!selectedCourseId) {
      alert("Please open a course first.");
      return;
    }

    // Find the course currently being viewed.
    const course = getSelectedCourse();

    // Build a student object from the form inputs.
    const student = {
      id: crypto.randomUUID(),
      name: document.getElementById("student-email-name").value.trim(),
      email: document.getElementById("student-email-input").value.trim(),
      courseId: selectedCourseId,
      status: "Invite Pending",
    };

    // Try to send the invite email.
    // For now, this calls a backend endpoint placeholder.
    const inviteResult = await sendStudentInviteEmail(student, course);

    if (!inviteResult.success) {
      alert("Invite failed: " + inviteResult.message);
      return;
    }

    // Add the student locally after the invite succeeds.
    teacherData.students.push(student);
    saveData();

    alert(inviteResult.message);

    // Clear the form and redraw the page.
    event.target.reset();
    renderSelectedCourse();
    renderDashboard();
  });

document
  .getElementById("student-request-form")
  .addEventListener("submit", (event) => {
    event.preventDefault();

    if (!selectedCourseId) {
      return;
    }

    const request = {
      id: crypto.randomUUID(),
      name: document.getElementById("request-student-name").value.trim(),
      courseId: selectedCourseId,
    };

    teacherData.joinRequests.push(request);
    saveData();

    event.target.reset();
    renderJoinRequests();
  });

document.getElementById("test-form").addEventListener("submit", (event) => {
  event.preventDefault();

  if (!selectedCourseId) {
    alert("Please open a course first.");
    return;
  }

  if (draftQuestions.length === 0) {
    alert("Please add at least one question before creating the test.");
    return;
  }

  const test = {
    id: crypto.randomUUID(),
    title: document.getElementById("test-title").value.trim(),
    courseId: selectedCourseId,
    type: document.getElementById("test-type").value,
    timeLimit: document.getElementById("test-time").value,
    difficulty: document.getElementById("test-difficulty").value,
    description: document.getElementById("test-description").value.trim(),
    questions: [...draftQuestions],
  };

  teacherData.tests.push(test);
  draftQuestions = [];

  saveData();

  event.target.reset();
  renderApp();
  showCourseTab("tests");
});

/**
 * Temporary demo version.
 * This does NOT send a real email.
 * It pretends the email was sent so the frontend flow keeps working.
 */
async function sendStudentInviteEmail(student, course) {
  console.log("Demo invite email:", {
    to: student.email,
    studentName: student.name,
    courseName: course ? course.name : "Unknown Course",
    courseCode: course ? course.code : "Unknown Code",
  });

  return {
    success: true,
    message:
      "Demo: invite email marked as sent. Backend email system not connected yet.",
  };
}

function acceptJoinRequest(requestId) {
  const request = teacherData.joinRequests.find(
    (request) => request.id === requestId,
  );

  if (!request) {
    return;
  }

  teacherData.students.push({
    id: crypto.randomUUID(),
    name: request.name,
    email: "pending-email@example.com",
    courseId: request.courseId,
    status: "Request Accepted",
  });

  teacherData.joinRequests = teacherData.joinRequests.filter(
    (request) => request.id !== requestId,
  );

  saveData();
  renderSelectedCourse();
  renderDashboard();
}

/* ============================================================
         HELPERS
         ============================================================ */

function getSelectedCourse() {
  return teacherData.courses.find((course) => course.id === selectedCourseId);
}

function getStudentsForCourse(courseId) {
  return teacherData.students.filter(
    (student) => student.courseId === courseId,
  );
}

function getTestsForCourse(courseId) {
  return teacherData.tests.filter((test) => test.courseId === courseId);
}

function getResultsForCourse(courseId) {
  return completedTests.filter((result) => result.courseId === courseId);
}

function clearDemoData() {
  if (!confirm("Clear all demo courses, students, and tests?")) {
    return;
  }

  localStorage.removeItem(STORAGE_KEY);
  teacherData = loadData();
  selectedCourseId = null;
  selectedTestId = null;
  draftQuestions = [];

  renderApp();
  showDashboard();
}

renderApp();

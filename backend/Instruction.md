### 1. Backend Environment Configuration

First, navigate to your project directory and set up your environment variables.

**Create the Configuration:**
Create a file named `.env` inside the `backend` folder and add the following:

```bash
GEMINI_API_KEY=your_real_key
GEMINI_MODEL=gemini-2.5-flash
PORT=8080
```

### 2. Compilation & Execution

Open your terminal and run these commands to compile the Java source and execute the startup script:

```bash
# 1. Compile the backend main class
javac backend/Application.java

# 2. Grant execution permissions and run the script
chmod +x ./backend/run.sh
./backend/run.sh
```

---

### 3. Terminal Connectivity Test

Once the backend is running, verify the connection to the Gemini API using `curl`:

```bash
curl -X POST http://localhost:8080/api/gemini \
  -H 'Content-Type: application/json' \
  -d '{"message":"Generate exactly 1 AP Computer Science A multiple-choice question. Difficulty level: 2 on a scale of 1 to 3. Topic: Java programming. Requirements: 4 answer choices labeled A, B, C, and D. Exactly 1 correct answer. Make it appropriate for a high school AP CSA student. Do not include markdown or extra explanation. Return valid JSON only in this format - not always answer A: {\"topic\":\"string\",\"text\":\"string\",\"choices\":[{\"id\":\"A\",\"text\":\"string\"},{\"id\":\"B\",\"text\":\"string\"},{\"id\":\"C\",\"text\":\"string\"},{\"id\":\"D\",\"text\":\"string\"}],\"correctChoice\":\"A\"}"}'

```

---

### 4. Frontend Implementation (TypeScript)

To integrate, use the following `fetch` logic:

```typescript
const response = await fetch("http://localhost:8080/api/gemini", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ message: userMessage }),
});

const data = await response.json();

if (!response.ok) {
    throw new Error(data.error || "Chat request failed");
}

console.log(data.reply);

```

---

> **Note**: Currently, this setup is strictly for **local development**. Production deployment to your domain would require a hosting instance and SSL configuration.

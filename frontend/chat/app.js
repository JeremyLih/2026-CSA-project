const DEFAULT_REMOTE_API_URL = "https://cs.andromedax.org/api/gemini";
const DEFAULT_LOCAL_API_URL = "http://localhost:8080/api/gemini";

const form = document.querySelector("#chat-form");
const input = document.querySelector("#message-input");
const messageList = document.querySelector("#message-list");
const sendButton = document.querySelector("#send-button");
const clearButton = document.querySelector("#clear-button");
const apiUrlLabel = document.querySelector("#api-url");

const apiUrl = resolveApiUrl();
apiUrlLabel.textContent = `POST ${apiUrl}`;

seedMessages();

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const message = input.value.trim();
  if (!message) {
    return;
  }

  pushMessage("user", message);
  input.value = "";
  setSending(true);

  try {
    const reply = await requestReply(message);
    pushMessage("assistant", reply);
  } catch (error) {
    pushMessage("system", error.message || "Request failed.");
  } finally {
    setSending(false);
    input.focus();
  }
});

input.addEventListener("keydown", (event) => {
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    form.requestSubmit();
  }
});

clearButton.addEventListener("click", () => {
  messageList.innerHTML = "";
  seedMessages();
  input.focus();
});

function resolveApiUrl() {
  const queryValue = new URLSearchParams(window.location.search).get("api");
  if (queryValue) {
    return queryValue;
  }

  const isLocalHost =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1";

  return isLocalHost ? DEFAULT_LOCAL_API_URL : DEFAULT_REMOTE_API_URL;
}

function seedMessages() {
  pushMessage(
    "assistant",
    "Ready. Send one message and this page will forward it to the Gemini backend."
  );
}

function pushMessage(role, text) {
  const item = document.createElement("article");
  item.className = `message message-${role}`;
  item.textContent = text;
  messageList.appendChild(item);
  messageList.scrollTop = messageList.scrollHeight;
}

async function requestReply(message) {
  const response = await fetch(apiUrl, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ message }),
  });

  let data = null;
  try {
    data = await response.json();
  } catch (error) {
    throw new Error("Backend did not return JSON.");
  }

  if (!response.ok) {
    throw new Error(data.error || "Chat request failed.");
  }

  if (!data.reply || typeof data.reply !== "string") {
    throw new Error("Backend response did not include reply.");
  }

  return data.reply;
}

function setSending(isSending) {
  sendButton.disabled = isSending;
  sendButton.textContent = isSending ? "Sending..." : "Send";
  input.disabled = isSending;
  clearButton.disabled = isSending;
}

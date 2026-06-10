# 🎙️ Voice AI Agent — Spring Boot

A real-time Voice AI Agent built with **Spring Boot**, **Twilio**, **Deepgram**, and **OpenAI GPT-4o-mini**. Supports browser-based voice conversations and bulk outbound calling.

---

## 🏗️ Architecture

```
Browser / Phone Call
        ↓
    Twilio (Call Handling + Audio Stream)
        ↓
  Spring Boot (WebSocket Bridge + Orchestrator)
        ↓
  Deepgram Voice Agent API
    ├── Listen  → Nova-2 (Speech to Text)
    ├── Think   → OpenAI GPT-4o-mini (LLM)
    └── Speak   → Aura Asteria (Text to Speech)
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2.x |
| Call Handling | Twilio Programmable Voice |
| STT | Deepgram Nova-2 |
| LLM | OpenAI GPT-4o-mini |
| TTS | Deepgram Aura (Asteria) |
| Real-time Audio | WebSocket (Spring WebSocket) |
| Local Tunneling | Ngrok |
| Frontend | HTML, CSS, Vanilla JS |

---

## ✨ Features

- ✅ Real-time voice conversation in browser
- ✅ Twilio phone call integration
- ✅ Deepgram Voice Agent API (STT + LLM + TTS in single WebSocket)
- ✅ Bulk outbound calling via REST API
- ✅ CSV upload for bulk call contact list
- ✅ Live conversation transcript in browser
- ✅ Hindi / Marathi language support

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/voiceai/agent/
│   │   ├── AgentApplication.java
│   │   └── controller/
│   │       ├── VoiceController.java         # Twilio webhook + bulk call endpoints
│   │       ├── TwilioMediaStreamHandler.java # WebSocket bridge (Twilio ↔ Deepgram)
│   │       ├── DeepgramAgentHandler.java     # Deepgram response handler
│   │       ├── BulkCallService.java          # Bulk calling logic
│   │       └── WebSocketConfig.java          # WebSocket configuration
│   └── resources/
│       ├── application.properties
│       └── static/
│           └── index.html                    # Browser test UI
```

---

## ⚙️ Setup & Configuration

### Prerequisites

- Java 17+
- Maven 3.8+
- [Ngrok](https://ngrok.com) account
- [Twilio](https://twilio.com) account with phone number
- [Deepgram](https://deepgram.com) API key
- [OpenAI](https://platform.openai.com) API key

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/voice-agent.git
cd voice-agent
```

### 2. Configure `application.properties`

```properties
spring.application.name=agent

twilio.account-sid=YOUR_TWILIO_ACCOUNT_SID
twilio.auth-token=YOUR_TWILIO_AUTH_TOKEN
twilio.phone-number=YOUR_TWILIO_PHONE_NUMBER

deepgram.api-key=YOUR_DEEPGRAM_API_KEY
openai.api-key=YOUR_OPENAI_API_KEY

server.port=8080
server.ngrok-url=https://YOUR_NGROK_URL.ngrok-free.app
```

> ⚠️ Never commit real API keys to GitHub. Use environment variables or `.env` file in production.

### 3. Run the Application

```bash
# Terminal 1 — Spring Boot
mvn spring-boot:run

# Terminal 2 — Ngrok
ngrok http 8080
```

### 4. Configure Twilio Webhook

```
Twilio Console → Phone Numbers → Active Numbers → Your Number
→ Voice Configuration → Webhook URL:
https://YOUR_NGROK_URL.ngrok-free.app/voice
HTTP: POST
```

---

## 🚀 Usage

### Browser Voice Test

```
http://localhost:8080
```

1. Click **Start Talking**
2. Allow microphone access
3. Speak — AI will respond in real-time

### Single Outbound Call

```
GET http://localhost:8080/make-call
```

### Bulk Outbound Calls — JSON

```bash
POST http://localhost:8080/bulk-call
Content-Type: application/json

[
  { "name": "Rahul Sharma", "phone": "+919876543210" },
  { "name": "Priya Patel",  "phone": "+919876543210" }
]
```

### Bulk Outbound Calls — CSV Upload

```bash
POST http://localhost:8080/bulk-call/csv
Content-Type: multipart/form-data

file: guests.csv
```

**CSV Format:**
```csv
name,phone
Rahul Sharma,+919876543210
Priya Patel,+919876543210
```

---

## 🔑 API Keys Setup

| Service | Link | Free Tier |
|---|---|---|
| Twilio | https://twilio.com | $15 trial credit |
| Deepgram | https://console.deepgram.com | $200 free credit |
| OpenAI | https://platform.openai.com | Pay per use |
| Ngrok | https://ngrok.com | Free static URL |

---

## 🌐 Deepgram Voice Agent Config

```json
{
  "type": "Settings",
  "audio": {
    "input":  { "encoding": "linear16", "sample_rate": 16000 },
    "output": { "encoding": "linear16", "sample_rate": 16000, "container": "none" }
  },
  "agent": {
    "listen": { "provider": { "type": "deepgram", "model": "nova-2" } },
    "think":  { "provider": { "type": "open_ai",  "model": "gpt-4o-mini" } },
    "speak":  { "provider": { "type": "deepgram", "model": "aura-asteria-en" } }
  }
}
```

---

## 📝 Environment Variables (Production)

```bash
export TWILIO_ACCOUNT_SID=ACxxxxxxxxxx
export TWILIO_AUTH_TOKEN=xxxxxxxxxx
export TWILIO_PHONE_NUMBER=+1XXXXXXXXXX
export DEEPGRAM_API_KEY=xxxxxxxxxx
export OPENAI_API_KEY=sk-xxxxxxxxxx
```

---

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first.

---

## 📄 License

MIT License — feel free to use and modify.

---

## 👨‍💻 Author

**Gaurav Nandan**  
Java Full Stack Developer  
Spring Boot | React | Voice AI  

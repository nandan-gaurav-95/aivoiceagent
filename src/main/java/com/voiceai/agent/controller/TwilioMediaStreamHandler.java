package com.voiceai.agent.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TwilioMediaStreamHandler extends AbstractWebSocketHandler {

    @Value("${deepgram.api-key}")
    private String deepgramApiKey;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    private final Map<String, WebSocketSession> deepgramSessions = new ConcurrentHashMap<>();
    private final Map<String, String> streamSidMap = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Twilio connected: " + session.getId());
        connectToDeepgram(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession twilioSession, TextMessage message) throws Exception {
        JsonNode json = mapper.readTree(message.getPayload());
        String event = json.path("event").asText();

        switch (event) {
            case "start" -> {
                String streamSid = json.path("start").path("streamSid").asText();
                streamSidMap.put(twilioSession.getId(), streamSid);
                System.out.println("Stream started: " + streamSid);
            }
            case "media" -> {
                // Twilio mulaw audio → Deepgram la forward kara
                String audioPayload = json.path("media").path("payload").asText();
                WebSocketSession dgSession = deepgramSessions.get(twilioSession.getId());
                if (dgSession != null && dgSession.isOpen()) {
                    byte[] audioBytes = Base64.getDecoder().decode(audioPayload);
                    dgSession.sendMessage(new BinaryMessage(audioBytes));
                }
            }
            case "stop" -> {
                System.out.println("Stream stopped");
                closeDeepgramSession(twilioSession.getId());
            }
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession twilioSession, BinaryMessage message) throws Exception {
        // Browser directly binary audio pathavto → Deepgram la forward kara
        WebSocketSession dgSession = deepgramSessions.get(twilioSession.getId());
        if (dgSession != null && dgSession.isOpen()) {
            dgSession.sendMessage(message);
            System.out.println("Audio forwarded: " + message.getPayload().remaining() + " bytes");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        closeDeepgramSession(session.getId());
    }

    private void connectToDeepgram(WebSocketSession twilioSession) {
        String deepgramWsUrl = "wss://agent.deepgram.com/v1/agent/converse";
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Token " + deepgramApiKey);

        DeepgramAgentHandler handler = new DeepgramAgentHandler(
                twilioSession, streamSidMap, openAiApiKey);

        client.execute(handler, headers, URI.create(deepgramWsUrl))
            .thenAccept(dgSession -> {
                deepgramSessions.put(twilioSession.getId(), dgSession);
                System.out.println("Connected to Deepgram Voice Agent");
                sendDeepgramConfig(dgSession);
            })
            .exceptionally(ex -> {
                System.err.println("Deepgram connection failed: " + ex.getMessage());
                return null;
            });
    }

    private void sendDeepgramConfig(WebSocketSession dgSession) {

        String config = """
            {
              "type": "Settings",
              "audio": {
                "input": {
                  "encoding": "linear16",
                  "sample_rate": 16000
                },
                "output": {
                  "encoding": "linear16",
                  "sample_rate": 16000,
                  "container": "none"
                }
              },
              "agent": {
                "language": "en",
                "listen": {
                  "provider": {
                    "type": "deepgram",
                    "model": "nova-2"
                  }
                },
                "think": {
                  "provider": {
                    "type": "open_ai",
                    "model": "gpt-4o-mini"
                  },
                  "instructions": "You are a female voice assistant for a wedding invitation. When call connects, immediately say: Namaste, main Sharma family ki taraf se bol rahi hoon. 
                  Aapko hamare ghar mein hone wali shadi mein aane ka nimantran hai. 
                  Shadi 15 July 2025 ko shaam 7 baje Mangal Karyalay Pune mein hai.
                    Always speak in Hindi."
                },
                "speak": {
                  "provider": {
                    "type": "deepgram",
                    "model": "aura-asteria-en"
                  }
                }
              }
            }
            """;

        try {
            dgSession.sendMessage(new TextMessage(config));
            System.out.println("Wedding config sent to Deepgram");
        } catch (IOException e) {
            System.err.println("Config send failed: " + e.getMessage());
        }
    }

    private void closeDeepgramSession(String sessionId) {
        WebSocketSession dgSession = deepgramSessions.remove(sessionId);
        if (dgSession != null && dgSession.isOpen()) {
            try {
                dgSession.close();
            } catch (IOException e) {
                System.err.println("Close error: " + e.getMessage());
            }
        }
        streamSidMap.remove(sessionId);
    }
}
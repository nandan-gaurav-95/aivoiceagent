package com.voiceai.agent.controller;

import java.util.Map;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeepgramAgentHandler extends AbstractWebSocketHandler {

    private final WebSocketSession clientSession;
    private final Map<String, String> streamSidMap;
    private final String openAiApiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public DeepgramAgentHandler(WebSocketSession clientSession,
                                 Map<String, String> streamSidMap,
                                 String openAiApiKey) {
        this.clientSession = clientSession;
        this.streamSidMap = streamSidMap;
        this.openAiApiKey = openAiApiKey;
    }

    @Override
    protected void handleTextMessage(WebSocketSession dgSession, TextMessage message) throws Exception {
        JsonNode json = mapper.readTree(message.getPayload());
        String type = json.path("type").asText();

        System.out.println("Deepgram message: " + message.getPayload());

        switch (type) {
            case "UserStartedSpeaking" ->
                System.out.println("User started speaking...");

            case "ConversationText" -> {
                String role = json.path("role").asText();
                String content = json.path("content").asText();
                System.out.println("[" + role + "]: " + content);

                // Browser la text message pan pathav — display sathi
                if (clientSession.isOpen()) {
                    clientSession.sendMessage(new TextMessage(message.getPayload()));
                }
            }

            case "AgentAudioDone" ->
                System.out.println("Agent finished speaking");
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession dgSession, BinaryMessage message) throws Exception {
        // Deepgram audio → Browser la directly pathav
        System.out.println("Deepgram audio received: " + message.getPayload().remaining() + " bytes → sending to browser");

        if (clientSession != null && clientSession.isOpen()) {
            // ByteBuffer copy karoon pathav
            java.nio.ByteBuffer audioData = message.getPayload();
            byte[] audioBytes = new byte[audioData.remaining()];
            audioData.get(audioBytes);

            clientSession.sendMessage(new BinaryMessage(audioBytes));
            System.out.println("Audio sent to browser: " + audioBytes.length + " bytes");
        } else {
            System.out.println("Client session closed — audio drop");
        }
    }
}
package Project.FishingNet_thesis.websocket.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collection;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("Connection established with session id: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("Connection closed with session id: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        System.out.println("Handling text message");
        if (message.getPayload() == null || message.getPayload().isEmpty()) {
            System.out.println("Received an empty message");
            return;
        }
        // give message to all clients
        for (WebSocketSession s : sessions.values()) {
            sendMessage(s, message.getPayload());
        }
    }

    public void sendMessage(WebSocketSession session, String message) {
        System.out.printf("Sending message to session id: %s\n", session.getId());
        try {
            session.sendMessage(new TextMessage(message));

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            JsonNode jsonNode = mapper.readTree(message);
            // print received message in JSON format
            System.out.println(mapper.writeValueAsString(jsonNode));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<WebSocketSession> getSessions() {
        return sessions.values();
    }
}
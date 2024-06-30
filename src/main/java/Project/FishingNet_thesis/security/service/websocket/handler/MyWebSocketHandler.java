package Project.FishingNet_thesis.security.service.websocket.handler;

import Project.FishingNet_thesis.models.DeviceDocument;
import Project.FishingNet_thesis.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collection;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private Map<String, WebSocketSession> frontendSessions = new ConcurrentHashMap<>();

    @Autowired
    private DeviceRepository deviceRepository;

    private String getTypeFromSession(WebSocketSession session) {
        // extract the type parameter from the session's URI
        try {
            String query = session.getUri().getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue[0].equals("type")) {
                    return keyValue[1];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getDeviceIdFromSession(WebSocketSession session) {
        try {
            // extract the device ID from the session's URI
            String query = session.getUri().getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue[0].equals("deviceId")) {
                    return keyValue[1];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String type = getTypeFromSession(session);
        if (type.equals("device")) {
            // this is a device connection
            sessions.put(session.getId(), session);
            System.out.println("Device connection established with session id: " + session.getId());
            String deviceId = getDeviceIdFromSession(session);
            Optional<DeviceDocument> device = deviceRepository.findById(deviceId);
            if (device.isPresent()) {
                device.get().setDeviceStatus(true);
                device.get().setWs_sessionID(session.getId());
                deviceRepository.save(device.get());
            }
        } else if (type.equals("frontend")) {
            // this is a frontend connection
            frontendSessions.put(session.getId(), session);
            System.out.println("Frontend connection established with session id: " + session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String type = getTypeFromSession(session);
        if (type.equals("device")) {
            // this is a device connection
            sessions.remove(session.getId());
            System.out.println("Device connection closed with session id: " + session.getId());
            String deviceId = getDeviceIdFromSession(session);
            Optional<DeviceDocument> device = deviceRepository.findById(deviceId);
            if (device.isPresent()) {
                device.get().setDeviceStatus(false);
                device.get().setWs_sessionID(null);
                deviceRepository.save(device.get());
            }
        } else if (type.equals("frontend")) {
            // this is a frontend connection
            frontendSessions.remove(session.getId());
            System.out.println("Frontend connection closed with session id: " + session.getId());
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        System.out.println("Handling text message");
        if (message.getPayload() == null || message.getPayload().isEmpty()) {
            System.out.println("Received an empty message");
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(message.getPayload());

            // Check if the message should be sent to frontend
            JsonNode sendToNode = jsonNode.get("sendTo");
            if (sendToNode != null && "frontend".equals(sendToNode.asText())) {
                // give message to all Frontend
                for (WebSocketSession s : frontendSessions.values()) {
                    sendMessage(s, message.getPayload());
                }
            } else {
                // give message to all Device
                for (WebSocketSession s : sessions.values()) {
                    sendMessage(s, message.getPayload());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
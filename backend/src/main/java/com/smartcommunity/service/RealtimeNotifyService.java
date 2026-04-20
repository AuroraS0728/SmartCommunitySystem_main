package com.smartcommunity.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealtimeNotifyService {

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> wsSessions = ConcurrentHashMap.newKeySet();
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    public void registerWebSocket(WebSocketSession session) {
        wsSessions.add(session);
    }

    public void unregisterWebSocket(WebSocketSession session) {
        wsSessions.remove(session);
    }

    public SseEmitter registerSse() {
        String emitterId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(30L * 60L * 1000L);
        sseEmitters.put(emitterId, emitter);
        emitter.onCompletion(() -> sseEmitters.remove(emitterId));
        emitter.onTimeout(() -> sseEmitters.remove(emitterId));
        emitter.onError(ex -> sseEmitters.remove(emitterId));
        sendSseEvent(emitter, "connected", Map.of("ts", LocalDateTime.now().toString()));
        return emitter;
    }

    public void publishDataChanged(String topic, String action, String path) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "data.changed");
        payload.put("topic", topic);
        payload.put("action", action);
        payload.put("path", path);
        payload.put("ts", LocalDateTime.now().toString());
        broadcast(payload);
    }

    public void broadcast(Object payload) {
        String text = toJson(payload);
        if (text == null) {
            return;
        }
        broadcastWebSocket(text);
        broadcastSse(payload);
    }

    private void broadcastWebSocket(String text) {
        for (WebSocketSession session : wsSessions) {
            if (session == null || !session.isOpen()) {
                wsSessions.remove(session);
                continue;
            }
            try {
                session.sendMessage(new TextMessage(text));
            } catch (IOException ex) {
                wsSessions.remove(session);
                log.debug("websocket send failed, remove session: {}", ex.getMessage());
            }
        }
    }

    private void broadcastSse(Object payload) {
        for (Map.Entry<String, SseEmitter> entry : sseEmitters.entrySet()) {
            SseEmitter emitter = entry.getValue();
            if (!sendSseEvent(emitter, "data.changed", payload)) {
                sseEmitters.remove(entry.getKey());
            }
        }
    }

    private boolean sendSseEvent(SseEmitter emitter, String event, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(event).data(payload));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            log.warn("realtime payload json encode failed: {}", ex.getMessage());
            return null;
        }
    }
}

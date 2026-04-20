package com.smartcommunity.config;

import com.smartcommunity.service.RealtimeNotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class NotifyWebSocketHandler extends TextWebSocketHandler {

    private final RealtimeNotifyService realtimeNotifyService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        realtimeNotifyService.registerWebSocket(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (session != null && session.isOpen() && "ping".equalsIgnoreCase(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        realtimeNotifyService.unregisterWebSocket(session);
    }
}

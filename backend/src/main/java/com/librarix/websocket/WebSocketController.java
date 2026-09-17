package com.librarix.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
public class WebSocketController {

    @MessageMapping("/ping")
    @SendTo("/topic/ping")
    public Map<String, Object> handlePing(Map<String, Object> payload) {
        log.info("Received WebSocket ping payload: {}", payload);
        return Map.of(
                "status", "PONG",
                "received", payload,
                "timestamp", System.currentTimeMillis()
        );
    }
}

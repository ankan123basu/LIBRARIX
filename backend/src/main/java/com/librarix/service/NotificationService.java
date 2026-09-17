package com.librarix.service;

import com.librarix.model.Notification;
import com.librarix.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Notification sendNotification(String userId, String title, String message, String type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        try {
            // Push per-user WebSocket event to STOMP destination /topic/user/{userId}
            String destination = "/topic/user/" + userId;
            messagingTemplate.convertAndSend(destination, saved);
            log.info("Pushed real-time WebSocket notification to {}: {}", destination, title);
        } catch (Exception ex) {
            log.warn("WebSocket push failed (user may not be connected yet): {}", ex.getMessage());
        }

        return saved;
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

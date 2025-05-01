package org.example.batchserver.java.batch.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.batchserver.java.batch.Notification.service.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationBatch {

    private final RestTemplate restTemplate;
    private final NotificationService notificationService;

    public void execute() {
        List<Map<String, Object>> notifications = notificationService.getPendingNotifications();

        if (!notifications.isEmpty()) {
            try {
                restTemplate.postForObject(
                        "http://main-server/api/internal/notifications/batch",
                        notifications,
                        Void.class
                );
                log.info("알림 {}건 전송 완료", notifications.size());
                notificationService.markAsSent(notifications);
            } catch (Exception e) {
                log.error("알림 전송 실패", e);
            }
        } else {
            log.info("전송할 알림 없음");
        }
    }
}
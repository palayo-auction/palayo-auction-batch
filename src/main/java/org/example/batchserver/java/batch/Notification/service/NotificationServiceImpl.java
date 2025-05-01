package org.example.batchserver.java.batch.Notification.service;

import lombok.RequiredArgsConstructor;
import org.example.batchserver.java.batch.Notification.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Map<String, Object>> getPendingNotifications() {
        return notificationRepository.findPendingNotifications();
    }

    @Override
    public void markAsSent(List<Map<String, Object>> notifications) {
        notificationRepository.updateNotificationStatusToSent(notifications);
    }
}

package org.example.batchserver.java.batch.Notification.service;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    List<Map<String, Object>> getPendingNotifications();
    void markAsSent(List<Map<String, Object>> notifications);
}


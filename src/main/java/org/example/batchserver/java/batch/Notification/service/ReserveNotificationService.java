package org.example.batchserver.java.batch.Notification.service;

import java.util.List;
import java.util.Map;

public interface ReserveNotificationService {
    void save(Map<String, Object> notification);
    List<Map<String, Object>> pollAll();
}

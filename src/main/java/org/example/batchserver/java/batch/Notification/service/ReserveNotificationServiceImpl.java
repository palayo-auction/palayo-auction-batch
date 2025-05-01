package org.example.batchserver.java.batch.Notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveNotificationServiceImpl implements ReserveNotificationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY = "notifications:reserved";

    @Override
    public void save(Map<String, Object> notification) {
        redisTemplate.opsForList().rightPush(KEY, notification);
    }

    @Override
    public List<Map<String, Object>> pollAll() {
        List<Map<String, Object>> notifications = new ArrayList<>();
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        while (true) {
            Object value = ops.leftPop(KEY);
            if (value == null) break;
            if (value instanceof Map<?, ?> map) {
                try {
                    notifications.add((Map<String, Object>) map);
                } catch (ClassCastException e) {
                    log.warn("알림 타입 불러오기 실패: {}", value);
                }
            }
        }
        return notifications;
    }
}

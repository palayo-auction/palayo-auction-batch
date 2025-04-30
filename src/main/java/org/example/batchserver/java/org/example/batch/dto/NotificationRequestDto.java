package org.example.batchserver.java.org.example.batch.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NotificationRequestDto {
    private Long userId;
    private String type;
    private String title;
    private String body;
    private Map<String, String> data;
    private LocalDateTime scheduledAt;
}

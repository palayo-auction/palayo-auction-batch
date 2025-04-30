package org.example.batchserver.java.org.example.batch.controller;

import lombok.RequiredArgsConstructor;
import org.example.batch.dto.NotificationRequestDto;
import org.example.batch.service.NotificationSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class NotificationJobController {

    private final NotificationSchedulerService notificationSchedulerService;

    @PostMapping
    public ResponseEntity<Map<String, String>> scheduleNotification(@RequestBody NotificationRequestDto request) {
        String jobId = notificationSchedulerService.scheduleNotification(request);
        return ResponseEntity.ok(Map.of("jobId", jobId));
    }
}

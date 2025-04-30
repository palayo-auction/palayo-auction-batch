package org.example.batchserver.java.org.example.batch.service;

import lombok.RequiredArgsConstructor;
import org.example.batch.dto.NotificationRequestDto;
import org.example.batch.quartz.GenericJob;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {

    private final Scheduler scheduler;

    public String scheduleNotification(NotificationRequestDto request) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("targetUrl", "http://localhost:8080/api/internal/notifications");
            jobDataMap.put("userId", request.getUserId());
            jobDataMap.put("type", request.getType());
            jobDataMap.put("title", request.getTitle());
            jobDataMap.put("body", request.getBody());
            jobDataMap.put("data", request.getData());

            JobDetail jobDetail = JobBuilder.newJob(GenericJob.class)
                    .withIdentity("job-" + UUID.randomUUID(), "notification-jobs")
                    .usingJobData(jobDataMap)
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .startAt(Date.from(request.getScheduledAt().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            return jobDetail.getKey().getName();
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule notification job", e);
        }
    }
}
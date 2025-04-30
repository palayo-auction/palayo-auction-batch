package org.example.batchserver.java.org.example.batch.service;

import lombok.RequiredArgsConstructor;
import org.example.batch.dto.JobRequestDto;
import org.example.batch.quartz.GenericJob;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobSchedulerService {

    private final Scheduler scheduler;

    public String scheduleJob(JobRequestDto request) {
        try {
            // Job 데이터 구성
            JobDataMap jobDataMap = new JobDataMap(request.getJobData());
            jobDataMap.put("jobName", request.getJobName());

            // JobDetail 생성
            JobDetail jobDetail = JobBuilder.newJob(GenericJob.class)
                    .withIdentity("job-" + UUID.randomUUID(), request.getJobGroup())
                    .usingJobData(jobDataMap)
                    .storeDurably()
                    .build();

            // Trigger 생성
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .startAt(Date.from(request.getScheduledAt().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                    .build();

            // Quartz 스케줄러에 등록
            scheduler.scheduleJob(jobDetail, trigger);

            return jobDetail.getKey().getName();  // jobId 반환
        } catch (SchedulerException e) {
            throw new RuntimeException("Job 스케줄링 실패", e);
        }
    }

    public void cancelJob(String jobId) {
        try {
            scheduler.deleteJob(new JobKey(jobId));
        } catch (SchedulerException e) {
            throw new RuntimeException("Job 삭제 실패", e);
        }
    }
}

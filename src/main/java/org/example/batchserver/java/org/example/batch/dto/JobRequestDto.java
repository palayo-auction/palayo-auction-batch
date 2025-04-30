package org.example.batchserver.java.org.example.batch.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class JobRequestDto {
    private String jobName;
    private String jobGroup;
    private LocalDateTime scheduledAt;
    private Map<String, Object> jobData;
}

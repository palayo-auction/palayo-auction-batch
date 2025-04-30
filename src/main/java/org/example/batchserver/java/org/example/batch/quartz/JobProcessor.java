package org.example.batchserver.java.org.example.batch.quartz;

import org.example.batch.Handler.JobHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JobProcessor {

    private final Map<String, JobHandler> handlers;

    public JobProcessor(Map<String, JobHandler> handlers) {
        this.handlers = handlers;
    }

    public void process(String jobName, Map<String, Object> jobData) {
        JobHandler handler = handlers.get(jobName);
        if (handler == null) {
            System.out.println("JOB헨들러: " + jobName);
            return;
        }
        handler.execute(jobData);
    }
}

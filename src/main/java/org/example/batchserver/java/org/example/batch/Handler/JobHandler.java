package org.example.batchserver.java.org.example.batch.Handler;

import java.util.Map;

public interface JobHandler {
    void execute(Map<String, Object> jobData);
}
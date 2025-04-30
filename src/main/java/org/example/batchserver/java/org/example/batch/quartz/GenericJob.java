package org.example.batchserver.java.org.example.batch.quartz;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class GenericJob extends QuartzJobBean {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        Map<String, Object> jobDataMap = context.getMergedJobDataMap();

        String targetUrl = (String) jobDataMap.get("targetUrl");
        if (targetUrl == null) {
            System.out.println("targetUrl이 없습니다. Job 실행 실패");
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", jobDataMap.get("userId"));
            payload.put("type", jobDataMap.get("type"));
            payload.put("title", jobDataMap.get("title"));
            payload.put("body", jobDataMap.get("body"));
            payload.put("data", jobDataMap.get("data"));
            payload.put("scheduledAt", jobDataMap.get("scheduledAt"));

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            restTemplate.postForEntity(targetUrl, entity, Void.class);

            System.out.println("본서버로 JSON 요청 성공: " + targetUrl);

        } catch (Exception e) {
            System.out.println("JSON 변환 또는 전송 실패: " + e.getMessage());
        }
    }
}
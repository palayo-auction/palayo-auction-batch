package org.example.batchserver.java.org.example.batch.Handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component("sendNotification")
@RequiredArgsConstructor
public class NotificationJobHandler implements JobHandler {

    private final RestTemplate restTemplate;

    @Value("${main-server.base-url}")
    private String mainServerBaseUrl;

    @Override
    public void execute(Map<String, Object> jobData) {
        String url = mainServerBaseUrl + "/api/internal/notifications";

        try {
            restTemplate.postForObject(url, jobData, Void.class);
            System.out.println("본서버에 알림 요청 성공");
        } catch (Exception e) {
            System.out.println("본서버 요청 실패: " + e.getMessage());
        }
    }
}

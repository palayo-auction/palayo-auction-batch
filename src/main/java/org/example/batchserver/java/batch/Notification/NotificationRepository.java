package org.example.batchserver.java.batch.Notification;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findPendingNotifications() {
        String sql = "SELECT id, fcm_token, title, body FROM notifications WHERE status = 'PENDING'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
                "id", rs.getLong("id"),
                "fcmToken", rs.getString("fcm_token"),
                "title", rs.getString("title"),
                "body", rs.getString("body")
        ));
    }

    public void updateNotificationStatusToSent(List<Map<String, Object>> notifications) {
        List<Object[]> params = notifications.stream()
                .map(n -> new Object[]{n.get("id")})
                .collect(Collectors.toList());

        String updateSql = "UPDATE notifications SET status = 'SENT' WHERE id = ?";
        jdbcTemplate.batchUpdate(updateSql, params);
    }
}

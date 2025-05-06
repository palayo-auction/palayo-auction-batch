package org.example.batchserver.java.batch.auction.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuctionScheduler {

	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;

	// 경매 업데이트 메서드
	public void updateAuctions() {
		// point_type 컬럼의 타입을 VARCHAR(255)로 수정
		alterPointTypeColumn();

		// 경매 상태 업데이트
		jdbcTemplate.update("""
            UPDATE auctions
            SET status = 'ACTIVE'
            WHERE status = 'READY' AND started_at <= NOW()
        """);

		// 만료된 경매의 ID 목록 조회
		List<Long> expiredAuctionIds = jdbcTemplate.queryForList("""
            SELECT id FROM auctions
            WHERE status = 'ACTIVE' AND expired_at <= NOW()
        """, Long.class);

		// 만료된 경매 처리
		for (Long auctionId : expiredAuctionIds) {
			assignWinnerAndCloseAuction(auctionId);
		}
	}

	// point_type 컬럼을 VARCHAR(255)로 수정하는 메서드
	private void alterPointTypeColumn() {
		jdbcTemplate.execute("ALTER TABLE point_histories MODIFY COLUMN point_type VARCHAR(255);");
	}

	// 경매 종료 및 낙찰자 처리
	private void assignWinnerAndCloseAuction(Long auctionId) {
		Connection conn = DataSourceUtils.getConnection(dataSource);
		try {
			conn.setAutoCommit(false); // 트랜잭션 시작

			JdbcTemplate txTemplate = new JdbcTemplate(dataSource);
			txTemplate.setDataSource(dataSource);

			List<Map<String, Object>> topBid = txTemplate.queryForList("""
                SELECT bidder_id, bid_price
                FROM auction_histories
                WHERE auction_id = ?
                ORDER BY bid_price DESC, created_at ASC
                LIMIT 1
            """, auctionId);

			if (topBid.isEmpty()) {
				txTemplate.update("""
                    UPDATE auctions
                    SET status = 'FAILED'
                    WHERE id = ?
                """, auctionId);

				conn.commit();
				return;
			}

			Long winnerId = ((Number) topBid.get(0).get("bidder_id")).longValue();
			int bidPrice = ((Number) topBid.get(0).get("bid_price")).intValue();

			Map<String, Object> auction = txTemplate.queryForMap("""
                SELECT starting_price, item_id
                FROM auctions
                WHERE id = ?
            """, auctionId);

			int startingPrice = ((Number) auction.get("starting_price")).intValue();
			Long itemId = ((Number) auction.get("item_id")).longValue();
			int deposit = (int) Math.ceil(startingPrice * 0.1);
			int additionalCharge = Math.max(0, bidPrice - deposit);

			// 경매 종료 상태 업데이트
			txTemplate.update("""
                UPDATE auctions
                SET status = 'SUCCESS', winning_bidder_id = ?, success_at = NOW()
                WHERE id = ?
            """, winnerId, auctionId);

			// 입찰자 보증금 처리
			txTemplate.update("""
                UPDATE deposit_histories
                SET status = 'USED'
                WHERE auction_id = ? AND user_id = ?
            """, auctionId, winnerId);

			// 추가 차감된 금액 처리 (낙찰가가 보증금을 초과한 경우)
			if (additionalCharge > 0) {
				txTemplate.update("""
                    UPDATE users
                    SET point_amount = point_amount - ?
                    WHERE id = ?
                """, additionalCharge, winnerId);

				// 차감 내역 point_histories에 기록
				txTemplate.update("""
                    INSERT INTO point_histories (user_id, amount, point_type, created_at)
                    VALUES (?, ?, 'USED', NOW())
                """, winnerId, additionalCharge);
			}

			// 판매자에게 포인트 지급
			Long sellerId = txTemplate.queryForObject("""
                SELECT seller_id
                FROM items
                WHERE id = ?
            """, Long.class, itemId);

			txTemplate.update("""
                UPDATE users
                SET point_amount = point_amount + ?
                WHERE id = ?
            """, bidPrice, sellerId);

			// 판매자에게 수령 내역 기록
			txTemplate.update("""
                INSERT INTO point_histories (user_id, amount, point_type, created_at)
                VALUES (?, ?, 'INCREASE', NOW())
            """, sellerId, bidPrice);

			// 유찰자 처리
			List<Long> failedBidders = txTemplate.queryForList("""
                SELECT DISTINCT bidder_id
                FROM auction_histories
                WHERE auction_id = ? AND bidder_id != ?
            """, Long.class, auctionId, winnerId);

			for (Long failedId : failedBidders) {
				txTemplate.update("""
                    UPDATE deposit_histories
                    SET status = 'REFUNDED'
                    WHERE auction_id = ? AND user_id = ?
                """, auctionId, failedId);

				txTemplate.update("""
                    UPDATE users
                    SET point_amount = point_amount + ?
                    WHERE id = ?
                """, deposit, failedId);

				// 유찰자 환불 내역 기록
				txTemplate.update("""
                    INSERT INTO point_histories (user_id, amount, point_type, created_at)
                    VALUES (?, ?, 'REFUND', NOW())
                """, failedId, deposit);
			}

			conn.commit(); // 트랜잭션 커밋

		} catch (Exception e) {
			try {
				conn.rollback(); // 롤백
			} catch (SQLException rollbackEx) {
				throw new RuntimeException("롤백 실패", rollbackEx);
			}
			throw new RuntimeException("경매 종료 처리 실패", e);
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}
}

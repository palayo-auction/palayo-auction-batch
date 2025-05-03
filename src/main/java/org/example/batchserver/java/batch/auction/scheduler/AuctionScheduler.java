package org.example.batchserver.java.batch.auction.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuctionScheduler {

	private final JdbcTemplate jdbcTemplate;

	public void updateAuctions() {
		// READY → ACTIVE
		jdbcTemplate.update("""
            UPDATE auctions
            SET status = 'ACTIVE'
            WHERE status = 'READY' AND started_at <= NOW() 
        """);

		// ACTIVE → SUCCESS / FAILED
		List<Long> expiredAuctionIds = jdbcTemplate.queryForList("""
            SELECT id FROM auctions
            WHERE status = 'ACTIVE' AND expired_at <= NOW()
        """, Long.class);

		for (Long auctionId : expiredAuctionIds) {
			assignWinnerAndCloseAuction(auctionId);
		}
	}

	private void assignWinnerAndCloseAuction(Long auctionId) {
		List<Map<String, Object>> topBid = jdbcTemplate.queryForList("""
            SELECT bidder_id FROM auction_histories
            WHERE auction_id = ?
            ORDER BY bid_price DESC, created_at ASC
            LIMIT 1
        """, auctionId);

		if (!topBid.isEmpty()) {
			Long bidderId = (Long) topBid.get(0).get("bidder_id");

			jdbcTemplate.update("""
                UPDATE auctions
                SET status = 'SUCCESS', winning_bidder_id = ?, success_at = NOW()
                WHERE id = ?
            """, bidderId, auctionId);

		} else {
			jdbcTemplate.update("""
                UPDATE auctions
                SET status = 'FAILED'
                WHERE id = ?
            """, auctionId);
		}
	}
}

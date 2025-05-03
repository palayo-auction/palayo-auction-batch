package org.example.batchserver.java.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.batchserver.java.batch.auction.scheduler.AuctionScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final AuctionScheduler auctionScheduler;

    @Scheduled(fixedRate = 60000)
    public void runBatchJob() {
        try {
            auctionScheduler.updateAuctions();

        } catch (Exception e) {
            log.error("배치 작업 중 오류 발생", e);
        }
    }
}
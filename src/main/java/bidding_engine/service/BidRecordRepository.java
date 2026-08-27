package bidding_engine.service;

import bidding_engine.model.BidRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface BidRecordRepository
        extends ReactiveCrudRepository<BidRecord, UUID> {

    Flux<BidRecord> findByAuctionIdOrderByOccurredAtDesc(UUID auctionId);
}
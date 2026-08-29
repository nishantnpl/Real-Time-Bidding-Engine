package bidding_engine.service;

import bidding_engine.model.BidHistoryItem;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class BidHistoryService {

    private final DatabaseClient databaseClient;

    public BidHistoryService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Flux<BidHistoryItem> findHistory(UUID auctionId) {
        return databaseClient.sql("""
                        SELECT
                            id,
                            auction_id,
                            bidder_id,
                            amount,
                            occurred_at
                        FROM public.bids
                        WHERE auction_id = :auctionId
                        ORDER BY occurred_at DESC
                        """)
                .bind("auctionId", auctionId)
                .map((row, metadata) -> new BidHistoryItem(
                        row.get("id", UUID.class),
                        row.get("auction_id", UUID.class),
                        row.get("bidder_id", String.class),
                        row.get("amount", BigDecimal.class),
                        row.get("occurred_at", Instant.class)
                ))
                .all();
    }
}
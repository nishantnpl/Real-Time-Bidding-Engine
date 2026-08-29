package bidding_engine.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
//one row from PostgreSQL
public record BidHistoryItem(
        UUID id,
        UUID auctionId,
        String bidderId,
        BigDecimal amount,
        Instant occurredAt
) {
}
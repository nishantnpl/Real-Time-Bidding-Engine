package bidding_engine.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BidAcceptedEvent(
        UUID eventId,
        UUID auctionId,
        String bidderId,
        BigDecimal amount,
        Instant occurredAt
) {
}
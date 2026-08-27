package bidding_engine.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Auction(
        UUID id,
        String title,
        BigDecimal currentHighestBid,
        String highestBidderId,
        Instant endsAt,
        long endsAtEpochMillis,
        AuctionStatus status
) {
    public Auction(
            UUID id,
            String title,
            BigDecimal currentHighestBid,
            String highestBidderId,
            Instant endsAt,
            AuctionStatus status
    ) {
        this(
                id,
                title,
                currentHighestBid,
                highestBidderId,
                endsAt,
                endsAt.toEpochMilli(),
                status
        );
    }
}
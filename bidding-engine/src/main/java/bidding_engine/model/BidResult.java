package bidding_engine.model;

import java.math.BigDecimal;
import java.util.UUID;

public record BidResult(
        boolean accepted,
        String reason,
        UUID auctionId,
        BigDecimal currentHighestBid,
        String highestBidderId,
        AuctionStatus status
) {
}
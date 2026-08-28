package bidding_engine.model;

import java.math.BigDecimal;
import java.util.UUID;

//This will be sent back over WebSocket after validation.
public record BidSocketResponse(
        String type,
        UUID auctionId,
        String bidderId,
        BigDecimal currentHighestBid,
        String reason
) {
}
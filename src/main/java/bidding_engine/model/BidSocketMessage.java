package bidding_engine.model;

import java.math.BigDecimal;
import java.util.UUID;
//Frontend will send this JSON to the WebSocket endpoint

public record BidSocketMessage(
        String type,
        UUID auctionId,
        String bidderId,
        BigDecimal amount
) {
}
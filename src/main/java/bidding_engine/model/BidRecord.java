package bidding_engine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("bids")
public record BidRecord(
        @Id
        UUID id,

        @Column("auction_id")
        UUID auctionId,

        @Column("bidder_id")
        String bidderId,

        BigDecimal amount,

        @Column("occurred_at")
        Instant occurredAt
) {
}
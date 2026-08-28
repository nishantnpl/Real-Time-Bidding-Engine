package bidding_engine.service;

import bidding_engine.model.BidAcceptedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
public class BidPersistenceConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(BidPersistenceConsumer.class);

    private final DatabaseClient databaseClient;

    public BidPersistenceConsumer(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @KafkaListener(topics = "auction-bids", groupId = "bid-persistence")
    public void persistBid(BidAcceptedEvent event) {
        logger.info(
                "Received bid event for persistence: eventId={}, auctionId={}, bidderId={}, amount={}",
                event.eventId(),
                event.auctionId(),
                event.bidderId(),
                event.amount()
        );

        databaseClient.sql("""
                        INSERT INTO public.bids
                            (id, auction_id, bidder_id, amount, occurred_at)
                        VALUES
                            (:id, :auctionId, :bidderId, :amount, :occurredAt)
                        """)
                .bind("id", event.eventId())
                .bind("auctionId", event.auctionId())
                .bind("bidderId", event.bidderId())
                .bind("amount", event.amount())
                .bind("occurredAt", event.occurredAt())
                .fetch()
                .rowsUpdated()
                .doOnNext(count -> logger.info(
                        "Inserted {} row into public.bids for event {}",
                        count,
                        event.eventId()
                ))
                .doOnError(error -> logger.error(
                        "Failed to insert bid {}",
                        event.eventId(),
                        error
                ))
                .block();
    }
}
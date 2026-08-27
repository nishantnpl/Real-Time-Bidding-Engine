package bidding_engine.service;

import bidding_engine.model.BidAcceptedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class BidEventProducer {

    private static final String AUCTION_BIDS_TOPIC = "auction-bids";

    private final KafkaTemplate<String, BidAcceptedEvent> kafkaTemplate;

    public BidEventProducer(KafkaTemplate<String, BidAcceptedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<?> publishBidAccepted(BidAcceptedEvent event) {
        return kafkaTemplate.send(
                AUCTION_BIDS_TOPIC,
                event.auctionId().toString(),
                event
        );
    }
}
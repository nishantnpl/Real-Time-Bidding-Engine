package bidding_engine.service;

import bidding_engine.model.BidAcceptedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class BidEventConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(BidEventConsumer.class);

    private final WebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public BidEventConsumer(
            WebSocketSessionRegistry sessionRegistry,
            ObjectMapper objectMapper
    ) {
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "auction-bids", groupId = "auction-broadcast")
    public void consumeBidAccepted(BidAcceptedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            sessionRegistry.broadcast(json)
                    .subscribe(
                            unused -> logger.info("Broadcast bid event {}", event.eventId()),
                            error -> logger.error("WebSocket broadcast failed", error)
                    );
        } catch (Exception exception) {
            logger.error("Could not serialize bid event", exception);
        }
    }
}
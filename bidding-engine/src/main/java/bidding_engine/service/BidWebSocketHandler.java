package bidding_engine.service;

import bidding_engine.model.BidRequest;
import bidding_engine.model.BidResult;
import bidding_engine.model.BidSocketMessage;
import bidding_engine.model.BidSocketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Component
public class BidWebSocketHandler implements WebSocketHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(BidWebSocketHandler.class);

    private final WebSocketSessionRegistry sessionRegistry;
    private final AuctionService auctionService;
    private final ObjectMapper objectMapper;

    public BidWebSocketHandler(
            WebSocketSessionRegistry sessionRegistry,
            AuctionService auctionService,
            ObjectMapper objectMapper
    ) {
        this.sessionRegistry = sessionRegistry;
        this.auctionService = auctionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        sessionRegistry.add(session);

        Mono<Void> receiveAndProcess = session.receive()
                .flatMap(message -> handleMessage(session, message.getPayloadAsText()))
                .then();

        return receiveAndProcess
                .doFinally(signal -> sessionRegistry.remove(session));
    }

    private Mono<Void> handleMessage(WebSocketSession session, String payload) {
        try {
            BidSocketMessage socketMessage =
                    objectMapper.readValue(payload, BidSocketMessage.class);

            if (!"PLACE_BID".equals(socketMessage.type())) {
                return sendResponse(
                        session,
                        new BidSocketResponse(
                                "ERROR",
                                socketMessage.auctionId(),
                                socketMessage.bidderId(),
                                null,
                                "Unsupported message type"
                        )
                );
            }

            BidRequest bidRequest = new BidRequest(
                    socketMessage.bidderId(),
                    socketMessage.amount()
            );

            return auctionService.placeBid(socketMessage.auctionId(), bidRequest)
                    .flatMap(result -> sendBidResult(session, result));

        } catch (Exception exception) {
            logger.warn("Invalid WebSocket message: {}", payload, exception);

            return sendResponse(
                    session,
                    new BidSocketResponse(
                            "ERROR",
                            null,
                            null,
                            null,
                            "Invalid bid message"
                    )
            );
        }
    }

    private Mono<Void> sendBidResult(WebSocketSession session, BidResult result) {
        BidSocketResponse response = new BidSocketResponse(
                result.accepted() ? "BID_ACCEPTED" : "BID_REJECTED",
                result.auctionId(),
                result.highestBidderId(),
                result.currentHighestBid(),
                result.reason()
        );

        return sendResponse(session, response);
    }

    private Mono<Void> sendResponse(
            WebSocketSession session,
            BidSocketResponse response
    ) {
        try {
            String json = objectMapper.writeValueAsString(response);
            return session.send(Mono.just(session.textMessage(json)));
        } catch (Exception exception) {
            logger.error("Could not send WebSocket response", exception);
            return Mono.empty();
        }
    }
}
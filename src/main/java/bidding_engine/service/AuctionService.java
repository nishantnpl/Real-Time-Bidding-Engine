package bidding_engine.service;

import bidding_engine.model.Auction;
import bidding_engine.model.BidAcceptedEvent;
import bidding_engine.model.BidRequest;
import bidding_engine.model.BidResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuctionService {

    private final RedisAuctionRepository redisAuctionRepository;
    private final BidEventProducer bidEventProducer;

    public AuctionService(
            RedisAuctionRepository redisAuctionRepository,
            BidEventProducer bidEventProducer
    ) {
        this.redisAuctionRepository = redisAuctionRepository;
        this.bidEventProducer = bidEventProducer;
    }

    public Flux<Auction> findAll() {
        return redisAuctionRepository.findAll();
    }

    public Mono<Auction> findById(UUID auctionId) {
        return redisAuctionRepository.findById(auctionId);
    }

    public Mono<BidResult> placeBid(UUID auctionId, BidRequest bidRequest) {
        return redisAuctionRepository.placeBidAtomically(
                        auctionId,
                        bidRequest.bidderId(),
                        bidRequest.amount().toPlainString()
                )
                .doOnNext(result -> {
                    if (result.accepted()) {
                        BidAcceptedEvent event = new BidAcceptedEvent(
                                UUID.randomUUID(),
                                result.auctionId(),
                                result.highestBidderId(),
                                result.currentHighestBid(),
                                Instant.now()
                        );

                        bidEventProducer.publishBidAccepted(event);
                    }
                });
    }
}
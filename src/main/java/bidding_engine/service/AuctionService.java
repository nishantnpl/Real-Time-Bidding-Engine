package bidding_engine.service;

import bidding_engine.model.Auction;
import bidding_engine.model.BidRequest;
import bidding_engine.model.BidResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class AuctionService {

    private final RedisAuctionRepository redisAuctionRepository;

    public AuctionService(RedisAuctionRepository redisAuctionRepository) {
        this.redisAuctionRepository = redisAuctionRepository;
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
        );
    }
}
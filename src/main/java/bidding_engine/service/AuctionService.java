package bidding_engine.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import bidding_engine.model.Auction;
import bidding_engine.model.AuctionStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import bidding_engine.model.BidRequest;

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

    public Mono<Auction> placeBid(UUID auctionId, BidRequest bidRequest) {
        return redisAuctionRepository.findById(auctionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Auction not found")))
                .flatMap(auction -> {
                    if (auction.status() != AuctionStatus.OPEN ||
                            Instant.now().isAfter(auction.endsAt())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT, "Auction is closed"));
                    }

                    if (bidRequest.amount().compareTo(auction.currentHighestBid()) <= 0) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Bid amount must be higher than the current highest bid"));
                    }

                    Auction updated = new Auction(
                            auction.id(),
                            auction.title(),
                            bidRequest.amount(),
                            bidRequest.bidderId(),
                            auction.endsAt(),
                            auction.status()
                    );

                    return redisAuctionRepository.save(updated);
                });
    }
}
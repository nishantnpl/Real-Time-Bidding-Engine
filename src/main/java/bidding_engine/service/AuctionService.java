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

@Service
public class AuctionService {

    private final Map<UUID, Auction> auctions = new ConcurrentHashMap<>();

    public AuctionService() {
        Auction laptopAuction = new Auction(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "MacBook Pro 14-inch",
                new BigDecimal("1200.00"),
                "user-001",
                Instant.now().plus(Duration.ofHours(2)),
                AuctionStatus.OPEN
        );

        Auction cameraAuction = new Auction(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Sony Alpha Camera",
                new BigDecimal("650.00"),
                "user-002",
                Instant.now().plus(Duration.ofHours(4)),
                AuctionStatus.OPEN
        );

        auctions.put(laptopAuction.id(), laptopAuction);
        auctions.put(cameraAuction.id(), cameraAuction);
    }

    public Flux<Auction> findAll() {
        return Flux.fromIterable(auctions.values());
    }

    public Mono<Auction> findById(UUID auctionId) {
        return Mono.justOrEmpty(auctions.get(auctionId));
    }
}
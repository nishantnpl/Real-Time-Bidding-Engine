package bidding_engine.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import bidding_engine.model.Auction;
import bidding_engine.model.AuctionStatus;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuctionSeedData {

    private final RedisAuctionRepository redisAuctionRepository;

    public AuctionSeedData(RedisAuctionRepository redisAuctionRepository) {
        this.redisAuctionRepository = redisAuctionRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedAuctions() {
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

        redisAuctionRepository.save(laptopAuction)
                .then(redisAuctionRepository.save(cameraAuction))
                .subscribe();
    }
}
package bidding_engine.service;

import java.util.UUID;

import bidding_engine.model.Auction;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class RedisAuctionRepository {

    private static final String AUCTION_KEY_PREFIX = "auction:";

    private final ReactiveRedisTemplate<String, Auction> redisTemplate;

    public RedisAuctionRepository(ReactiveRedisTemplate<String, Auction> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Auction> save(Auction auction) {
        return redisTemplate.opsForValue()
                .set(key(auction.id()), auction)
                .thenReturn(auction);
    }

    public Mono<Auction> findById(UUID auctionId) {
        return redisTemplate.opsForValue()
                .get(key(auctionId));
    }

    public Flux<Auction> findAll() {
        return redisTemplate.keys(AUCTION_KEY_PREFIX + "*")
                .flatMap(key -> redisTemplate.opsForValue().get(key));
    }

    private String key(UUID auctionId) {
        return AUCTION_KEY_PREFIX + auctionId;
    }
}
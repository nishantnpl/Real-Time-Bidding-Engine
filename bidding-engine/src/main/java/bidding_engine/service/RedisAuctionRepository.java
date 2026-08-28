package bidding_engine.service;

import bidding_engine.model.Auction;
import bidding_engine.model.BidResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Repository
public class RedisAuctionRepository {

    private static final String AUCTION_KEY_PREFIX = "auction:";

    private final ReactiveRedisTemplate<String, Auction> redisTemplate;
    private final ReactiveRedisTemplate<String, String> stringRedisTemplate;
    private final DefaultRedisScript<String> placeBidScript;
    private final ObjectMapper objectMapper;

    public RedisAuctionRepository(
            ReactiveRedisTemplate<String, Auction> redisTemplate,
            ReactiveRedisTemplate<String, String> stringRedisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;

        this.placeBidScript = new DefaultRedisScript<>();
        this.placeBidScript.setLocation(new ClassPathResource("redis/place-bid.lua"));
        this.placeBidScript.setResultType(String.class);
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

    public Mono<BidResult> placeBidAtomically(
            UUID auctionId,
            String bidderId,
            String amount
    ) {
        return stringRedisTemplate.execute(
                        placeBidScript,
                        Collections.singletonList(key(auctionId)),
                        auctionId.toString(),
                        bidderId,
                        amount,
                        String.valueOf(Instant.now().toEpochMilli())
                )
                .single()
                .map(this::decodeBidResult);
    }

    private BidResult decodeBidResult(String json) {
        try {
            return objectMapper.readValue(json, BidResult.class);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not decode Redis bid result",
                    exception
            );
        }
    }

    private String key(UUID auctionId) {
        return AUCTION_KEY_PREFIX + auctionId;
    }
}
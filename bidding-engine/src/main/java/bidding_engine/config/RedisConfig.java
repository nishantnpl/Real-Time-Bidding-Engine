package bidding_engine.config;

import bidding_engine.model.Auction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Auction> auctionRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        RedisSerializer<Auction> auctionSerializer = new JacksonJsonRedisSerializer<>(Auction.class);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisSerializationContext<String, Auction> context = RedisSerializationContext
                .<String, Auction>newSerializationContext(stringSerializer)
                .value(auctionSerializer)
                .hashKey(stringSerializer)
                .hashValue(auctionSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
package bidding_engine.config;

import bidding_engine.service.BidWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;
//Exposes ws://localhost:8080/ws/auctions

@Configuration
public class WebSocketConfig {

    @Bean
    public SimpleUrlHandlerMapping webSocketMapping(
            BidWebSocketHandler bidWebSocketHandler
    ) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();

        mapping.setUrlMap(Map.of(
                "/ws/auctions", bidWebSocketHandler
        ));

        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
package bidding_engine.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class BidWebSocketHandler implements WebSocketHandler {

    private final WebSocketSessionRegistry sessionRegistry;

    public BidWebSocketHandler(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        sessionRegistry.add(session);

        return session.receive()
                .doFinally(signal -> sessionRegistry.remove(session))
                .then();
    }
}
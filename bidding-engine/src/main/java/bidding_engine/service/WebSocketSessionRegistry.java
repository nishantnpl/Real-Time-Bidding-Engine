package bidding_engine.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//Stores all open WebSocket connections in a thread-safe set

@Component
public class WebSocketSessionRegistry {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void add(WebSocketSession session) {
        sessions.add(session);
    }

    public void remove(WebSocketSession session) {
        sessions.remove(session);
    }

    public Mono<Void> broadcast(String message) {
        return Flux.fromIterable(sessions)
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> sendTo(session, message))
                .then();
    }

    private Mono<Void> sendTo(WebSocketSession session, String message) {
        WebSocketMessage webSocketMessage = session.textMessage(message);
        return session.send(Mono.just(webSocketMessage));
    }
}
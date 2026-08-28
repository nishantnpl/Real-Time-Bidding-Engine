package bidding_engine.controller;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DatabaseClient databaseClient;

    public HealthController(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "real-time-bidding-engine"
        );
    }
    @GetMapping("/bid-count")
    public Mono<Long> bidCount() {
        return databaseClient.sql("SELECT COUNT(*) AS count FROM public.bids")
                .fetch()
                .one()
                .map(row -> ((Number) row.get("count")).longValue());
    }

    @GetMapping("/database-info")
    public Mono<Map<String, Object>> databaseInfo() {
        return databaseClient.sql("""
                SELECT
                    current_database() AS database_name,
                    current_user AS database_user,
                    current_schema() AS schema_name,
                    inet_server_addr() AS server_address,
                    inet_server_port() AS server_port
                """)
                .fetch()
                .one();
    }
}
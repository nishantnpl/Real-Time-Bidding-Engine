package bidding_engine.controller;

import java.util.UUID;

import bidding_engine.model.Auction;
import bidding_engine.service.AuctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping
    public Flux<Auction> findAll() {
        return auctionService.findAll();
    }

    @GetMapping("/{auctionId}")
    public Mono<ResponseEntity<Auction>> findById(@PathVariable UUID auctionId) {
        return auctionService.findById(auctionId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
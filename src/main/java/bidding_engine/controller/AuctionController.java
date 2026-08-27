package bidding_engine.controller;

import bidding_engine.model.Auction;
import bidding_engine.model.BidRequest;
import bidding_engine.service.AuctionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping
    public Flux<Auction> getAllAuctions() {
        return auctionService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Auction>> getAuctionById(@PathVariable UUID id) {
        return auctionService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/bids")
    public Mono<ResponseEntity<Auction>> placeBid(
            @PathVariable UUID id,
            @RequestBody BidRequest bidRequest
    ) {
        return auctionService.placeBid(id, bidRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(
                        org.springframework.web.server.ResponseStatusException.class,
                        ex -> Mono.just(ResponseEntity
                                .status(ex.getStatusCode())
                                .build())
                );
    }
}
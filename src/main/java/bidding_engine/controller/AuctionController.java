package bidding_engine.controller;

import bidding_engine.model.Auction;
import bidding_engine.model.BidRequest;
import bidding_engine.model.BidResult;
import bidding_engine.service.AuctionService;
import jakarta.validation.Valid;
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

    @GetMapping("/{auctionId}")
    public Mono<ResponseEntity<Auction>> getAuctionById(
            @PathVariable UUID auctionId
    ) {
        return auctionService.findById(auctionId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{auctionId}/bids")
    public Mono<ResponseEntity<BidResult>> placeBid(
            @PathVariable UUID auctionId,
            @Valid @RequestBody BidRequest bidRequest
    ) {
        return auctionService.placeBid(auctionId, bidRequest)
                .map(result -> result.accepted()
                        ? ResponseEntity.ok(result)
                        : ResponseEntity.status(HttpStatus.CONFLICT).body(result));
    }
}
package bidding_engine.controller;

import bidding_engine.model.Auction;
import bidding_engine.model.BidHistoryItem;
import bidding_engine.model.BidRequest;
import bidding_engine.model.BidResult;
import bidding_engine.service.AuctionService;
import bidding_engine.service.BidHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;
    private final BidHistoryService bidHistoryService;

    public AuctionController(
            AuctionService auctionService,
            BidHistoryService bidHistoryService
    ) {
        this.auctionService = auctionService;
        this.bidHistoryService = bidHistoryService;
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

    @GetMapping("/{auctionId}/history")
    public Flux<BidHistoryItem> getBidHistory(
            @PathVariable UUID auctionId
    ) {
        return bidHistoryService.findHistory(auctionId);
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
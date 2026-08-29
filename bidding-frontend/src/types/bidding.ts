export interface Auction {
    id: string;
    title: string;
    currentHighestBid: number;
    highestBidderId: string;
    endsAt: string;
    endsAtEpochMillis: number;
    status: "OPEN" | "CLOSED";
}

export interface BidAcceptedEvent {
    eventId: string;
    auctionId: string;
    bidderId: string;
    amount: number;
    occurredAt: string;
}

export interface BidSocketResponse {
    type: "BID_ACCEPTED" | "BID_REJECTED" | "ERROR";
    auctionId: string | null;
    bidderId: string | null;
    currentHighestBid: number | null;
    status: "OPEN" | "CLOSED" | null;
    reason: string;
}

export interface PlaceBidSocketMessage {
    type: "PLACE_BID";
    auctionId: string;
    bidderId: string;
    amount: number;
}
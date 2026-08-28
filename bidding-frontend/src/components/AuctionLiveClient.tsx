"use client";

import { FormEvent, useEffect, useRef, useState } from "react";
import {
    Auction,
    BidAcceptedEvent,
    BidSocketResponse,
    PlaceBidSocketMessage
} from "@/types/bidding";

interface AuctionLiveClientProps {
    auctionId: string;
}

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const WS_URL = process.env.NEXT_PUBLIC_WS_URL ?? "ws://localhost:8080";

export default function AuctionLiveClient({
                                              auctionId
                                          }: AuctionLiveClientProps) {
    const [auction, setAuction] = useState<Auction | null>(null);
    const [connectionStatus, setConnectionStatus] =
        useState("Connecting");
    const [bidderId, setBidderId] = useState("user-012");
    const [amount, setAmount] = useState("1700");
    const [lastMessage, setLastMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const socketRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        let cancelled = false;

        async function loadAuction() {
            const url = `${API_URL}/api/auctions/${auctionId}`;

            try {
                console.log("Loading auction from:", url);

                const response = await fetch(url, {
                    method: "GET",
                    cache: "no-store"
                });

                if (!response.ok) {
                    throw new Error(
                        `Could not load auction. HTTP status: ${response.status}`
                    );
                }

                const data: Auction = await response.json();

                if (!cancelled) {
                    setAuction(data);
                    setErrorMessage("");
                }
            } catch (error) {
                console.error("Auction request failed for URL:", url, error);

                if (!cancelled) {
                    setErrorMessage(`Could not load the auction from ${url}`);
                }
            }
        }

        loadAuction();

        const socket = new WebSocket(`${WS_URL}/ws/auctions`);

        socket.onopen = () => {
            if (!cancelled) {
                socketRef.current = socket;
                setConnectionStatus("Connected");
            }
        };

        socket.onmessage = (event) => {
            if (cancelled) {
                return;
            }

            setLastMessage(event.data);

            try {
                const parsed = JSON.parse(event.data);

                if (isBidAcceptedEvent(parsed)) {
                    handleBidAcceptedEvent(parsed);
                    return;
                }

                if (isBidSocketResponse(parsed)) {
                    handleBidSocketResponse(parsed);
                }
            } catch (error) {
                console.error("Could not parse WebSocket message", error);
            }
        };

        socket.onerror = () => {
            if (!cancelled) {
                setConnectionStatus("Error");
            }
        };

        socket.onclose = () => {
            if (!cancelled) {
                setConnectionStatus("Disconnected");

                if (socketRef.current === socket) {
                    socketRef.current = null;
                }
            }
        };

        return () => {
            cancelled = true;

            if (socketRef.current === socket) {
                socketRef.current = null;
            }

            socket.close();
        };
    }, [auctionId]);



    function handleBidAcceptedEvent(event: BidAcceptedEvent) {
        setAuction((currentAuction) => {
            if (!currentAuction || currentAuction.id !== event.auctionId) {
                return currentAuction;
            }

            return {
                ...currentAuction,
                currentHighestBid: event.amount,
                highestBidderId: event.bidderId
            };
        });
    }

    function handleBidSocketResponse(response: BidSocketResponse) {
        if (response.type === "BID_ACCEPTED") {
            setErrorMessage("");
            return;
        }

        setErrorMessage(response.reason);
    }

    function submitBid(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setErrorMessage("");

        const numericAmount = Number(amount);

        if (!bidderId.trim()) {
            setErrorMessage("Bidder ID is required");
            return;
        }

        if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
            setErrorMessage("Amount must be greater than zero");
            return;
        }

        const socket = socketRef.current;

        if (!socket || socket.readyState !== WebSocket.OPEN) {
            setErrorMessage("WebSocket is not connected");
            return;
        }

        const message: PlaceBidSocketMessage = {
            type: "PLACE_BID",
            auctionId,
            bidderId: bidderId.trim(),
            amount: numericAmount
        };

        socket.send(JSON.stringify(message));
    }

    return (
        <main className="page">
            <section className="auction-card">
                <p className="eyebrow">Real-Time Bidding Engine</p>

                <h1>{auction?.title ?? "Loading auction..."}</h1>

                <p className={`status ${connectionStatus.toLowerCase()}`}>
                    WebSocket: {connectionStatus}
                </p>

                {auction && (
                    <>
                        <p className="price">
                            €{auction.currentHighestBid.toFixed(2)}
                        </p>
                        <p className="highest-bidder">
                            Highest bidder: {auction.highestBidderId}
                        </p>
                        <p className="ends-at">
                            Ends at: {new Date(auction.endsAt).toLocaleString()}
                        </p>
                        <p className="auction-status">
                            Auction status: {auction.status}
                        </p>
                    </>
                )}

                <form onSubmit={submitBid} className="bid-form">
                    <label>
                        Bidder ID
                        <input
                            value={bidderId}
                            onChange={(event) =>
                                setBidderId(event.target.value)
                            }
                            placeholder="user-012"
                        />
                    </label>

                    <label>
                        Bid amount
                        <input
                            type="number"
                            min="0"
                            step="0.01"
                            value={amount}
                            onChange={(event) =>
                                setAmount(event.target.value)
                            }
                            placeholder="1700.00"
                        />
                    </label>

                    <button
                        type="submit"
                        disabled={connectionStatus !== "Connected"}
                    >
                        Place Bid
                    </button>
                </form>

                {errorMessage && (
                    <p className="error">{errorMessage}</p>
                )}

                {lastMessage && (
                    <details className="message-details">
                        <summary>Latest WebSocket message</summary>
                        <pre>{lastMessage}</pre>
                    </details>
                )}
            </section>
        </main>
    );
}

function isBidAcceptedEvent(value: unknown): value is BidAcceptedEvent {
    if (typeof value !== "object" || value === null) {
        return false;
    }

    const event = value as Partial<BidAcceptedEvent>;

    return (
        typeof event.eventId === "string" &&
        typeof event.auctionId === "string" &&
        typeof event.bidderId === "string" &&
        typeof event.amount === "number" &&
        typeof event.occurredAt === "string"
    );
}

function isBidSocketResponse(value: unknown): value is BidSocketResponse {
    if (typeof value !== "object" || value === null) {
        return false;
    }

    const response = value as Partial<BidSocketResponse>;

    return (
        typeof response.type === "string" &&
        typeof response.reason === "string"
    );
}
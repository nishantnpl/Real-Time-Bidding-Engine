import AuctionLiveClient from "@/components/AuctionLiveClient";

const DEFAULT_AUCTION_ID =
    "11111111-1111-1111-1111-111111111111";

export default function Home() {
  return <AuctionLiveClient auctionId={DEFAULT_AUCTION_ID} />;
}
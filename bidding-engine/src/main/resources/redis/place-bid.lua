local auctionJson = redis.call('GET', KEYS[1])

if not auctionJson then
    return cjson.encode({
        accepted = false,
        reason = "Auction not found",
        auctionId = ARGV[1]
    })
end

local auction = cjson.decode(auctionJson)
local bidAmount = tonumber(ARGV[3])
local currentBid = tonumber(auction.currentHighestBid)
local nowMillis = tonumber(ARGV[4])
local endsAtMillis = tonumber(auction.endsAtEpochMillis)

if auction.status ~= "OPEN" or nowMillis >= endsAtMillis then
    if auction.status ~= "CLOSED" then
        auction.status = "CLOSED"
        redis.call('SET', KEYS[1], cjson.encode(auction))
    end

    return cjson.encode({
        accepted = false,
        reason = "Auction is closed",
        auctionId = auction.id,
        currentHighestBid = auction.currentHighestBid,
        highestBidderId = auction.highestBidderId,
        status = auction.status
    })
end

if bidAmount <= currentBid then
    return cjson.encode({
        accepted = false,
        reason = "Bid amount must be higher than the current highest bid",
        auctionId = auction.id,
        currentHighestBid = auction.currentHighestBid,
        highestBidderId = auction.highestBidderId,
        status = auction.status
    })
end

auction.currentHighestBid = bidAmount
auction.highestBidderId = ARGV[2]

redis.call('SET', KEYS[1], cjson.encode(auction))

return cjson.encode({
    accepted = true,
    reason = "Bid accepted",
    auctionId = auction.id,
    currentHighestBid = auction.currentHighestBid,
    highestBidderId = auction.highestBidderId,
    status = auction.status
})
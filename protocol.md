# CS410 Card Game Protocol

This document outlines the protocol for the card game server and client.

## Packet structure

A single TCP server is opened on port 6789. Packet payload is a JSON object encoded as a UTF-8 string.

## Card IDs

Cards are represented as 8-bit unsigned integers, where the first four bits represent the rank, and the last four bits represent the suit.

```
Hex: 0xC3
Bin: 11000011
     1100 (Queen)
         0011 (Hearts)
```

### Ranks
|Binary|Hex|Card Rank|
|---|---|---|
|`0010`|`0x2`|2|
|`0011`|`0x3`|3|
|`0100`|`0x4`|4|
|`0101`|`0x5`|5|
|`0110`|`0x6`|6|
|`0111`|`0x7`|7|
|`1000`|`0x8`|8|
|`1001`|`0x9`|9|
|`1010`|`0xA`|10|
|`1011`|`0xB`|J|
|`1100`|`0xC`|Q|
|`1101`|`0xD`|K|
|`1110`|`0xE`|A|

### Suits

|Binary|Hex|Card Suit|
|---|---|---|
|`0001`|`0x1`|Clubs|
|`0010`|`0x2`|Diamonds|
|`0011`|`0x3`|Hearts|
|`0100`|`0x4`|Spades|

## Connection

A client may connect to a server regardless of whether a game is in progress.
If they connect during a game, they are provided the hand of the computer-driven placeholder player.

## Server message types

### game_ready

Sent when the server is preparing to start a new game.
After this packet is sent out and before the game starts, no players may join the server.

```json
{
    "msgtype": "game_ready",
    "countdown": 3, // Countdown timer, starts immediately upon receipt
}
```

### game_start

Sent when the server starts the game. Each client is assigned a player ID,
hand, and some basic information on other players. The packet each client receives only contains information on their own hand.

```json
{
    "msgtype": "game_start",
    "client_info": {
        "player_index": 1, // Client becomes Player 2
        "hand": [ ... ], // Array of card IDs
    }
}
```

## Client message types

### play_card

Sent when a player plays a card from their hand.

```json
{
    "msgtype": "play_card",
    "card_index": 1, // Second card in hand
    "card_id": 179 // Jack of Hearts
}
```
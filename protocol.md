# CS410 Card Game Protocol

This document outlines the protocol for the card game server and client.

## Packet structure

A single TCP server is opened on port 6789. 
Packet payload is a single JSON object encoded as a UTF-8 string.

## Card IDs

Cards are represented as 8-bit unsigned integers, where the first four bits represent the rank, and the last four bits represent the suit.

```
Text: Queen of Hearts
Dec: 195
Hex: 0xC3
Bin: 11000011
     ||||||||
     1100|||| (1100 = Queen)
         0011 (0011 = Hearts)
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

### game_state

Contains all information about the current game state and can be sent
to any client at any time.

```json
{
    "msg_type": "game_state",
    "round": 12, // 1-based
    // playing = game in progress
    // ended = game has ended
    "game_state": "playing",
    "players": {
        "0": {
            "hand_size": 12,
            "points": 3
        },
        "1": {
            "hand_size": 12,
            "points": 8
        }
    },
    "turn": 0,
    // Current cards on the table. 0 = no card for player
    // Indices correspond to player indices
    "table": [0, 0, 0]
}
```

### client_reject

Sent when a player attempts to connect to the server but they are rejected
for some reason. The server disconnects the client right after sending this packet.

Sent in place of `game_state` packet when player connects. After packet
is sent, client is disconnected.

#### Rejection reason strings:

* `server_full`: The server is full and cannot accept any more players.
* `whitelist`: The client IP was not found on the server's whitelist.

```json
{
    "msg_type": "client_reject",
    "reject_reason": "server_full"
}
```

### client_info

Informs the client of the cards available in their hand.

```json
{
    "msg_type": "client_hand",
    "player_id": 0,
    "cards": [ ... ]
}
```

### client_move

Sent when any player plays a card. Contains information about the player
who played the card, the card played, and the result of the play.

When sent to the client who played the card, that client will update
their local hand.

```json
{
    "msg_type": "client_move",
    "player_index": 1, // Player 2 plays a card
    "card": 132, // 8 of Spades
    "round": 3, // Round number that the play is for
    "next_round": 3, // Round after play
    "next_turn": 2, // Index of next player to go
    // Result of play
    // -1 if no action, otherwise player index of round winner
    "result_type": -1
}
```

### player_score

Sent when a score for a player has been updated.

```json
{
    "msg_type": "player_score",
    "player_index": 1, // Player 2
    "wins": 3,
    "points": 34
}
```

### game_end

Sent when the game has ended and winner(s) have been chosen.

```json
{
    "msg_type": "game_end",
    // # of players tied for first
    "winner_count": 1,
    // Scoreboard sorted in descending order of wins, then points
    "scoreboard": [
        {
            "id": 2,
            "wins": 7,
            "points": 85
        },
        {
            "id": 1,
            "wins": 7,
            "points": 78
        },
        {
            "id": 3,
            "wins": 3,
            "points": 30
        }
    ],
    // # of ms until new game
    "delay_ms": 15000
}
```

### new_game_votes

Sent when any player votes to start a new game and contains information
about which players have voted so far.

```json
{
    "msg_type": "new_game_votes",
    "vote_state": [true, false, true] // P1 and P3 have voted; P2 has not
}
```

## Client message types

### play_card

Sent when a player plays a card from their hand.

```json
{
    "msg_type": "play_card",
    "card_index": 1, // Second card in hand
    "card_id": 179 // Jack of Hearts
}
```

### vote_new_game

Sent when, at game-over state, a player requests to start a new game.

```json
{
    "msg_type": "vote_new_game"
}
```
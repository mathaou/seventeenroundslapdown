using CardGameServer.Cards;
using System;
using System.Collections.Generic;
using System.Linq;

namespace CardGameServer
{
    public sealed class Game
    {
        private readonly List<Player> _players;
        private bool _active = false;
        private readonly Random _rng;
        private readonly Card?[] _roundCards;

        public Game(GameSettings settings) //initiate game server and list of players
        {
            Settings = settings;
            _rng = new Random();
            Server = new GameServer();
            _players = new List<Player>();
            _roundCards = new Card?[settings.MaxPlayers];
            for(int i = 0; i < MaxPlayers; i++)
            {
                var p = new Player(this, i);
                p.PlayingCard += OnPlayingCard;
                _players.Add(p);
            }
        }

        public int MaxPlayers => Settings.MaxPlayers;

        public GameServer Server { get; }

        public int Round { get; set; }

        public int TurnIndex { get; set; }

        public int LeadingPlayerId { get; set; }

        public GameSettings Settings { get; }

        public Random RNG => _rng;

        public CardSuit? LeadingSuit => _roundCards[LeadingPlayerId]?.Suit;

        public bool Start() //start server
        {
            if (_active) return false;

            Console.WriteLine("Starting game...");

            if (!Server.Start()) return false;
            
            Server.ClientConnected += OnClientConnected;
            Server.ClientDisconnected += OnClientDisconnected;

            NewGame();

            Console.WriteLine("Game ready");

            _active = true;
            return true;
        }

        public bool Stop()
        {
            if (!_active) return false;
            Console.WriteLine("Stopping...");
            Server.Stop();
            _active = false;
            Console.WriteLine("Stopped");
            return true;
        }

        public void NewGame()
        {
            // Generate deck
            var deck = Card.GeneratePile(_rng);
            int handSize = deck.Count / MaxPlayers;

            // Set up players
            foreach(var player in _players)
            {
                player.Score = 0;
                player.ClearHand();
                player.AddToHand(deck.GetRange(handSize * player.Id, handSize));
                Console.WriteLine($"Player {player.Id + 1} hand: {player.GetCards().Select(c => c.ToString()).Aggregate((c, n) => $"{c}, {n}")}\n");
            }

            Round = 0;
            TurnIndex = 0;
            LeadingPlayerId = 0;
            if (Server.ConnectedPlayerCount > 0) PromptCurrentPlayer();
        }
        

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            foreach (var player in _players)
            {
                if (player.Client == e.Client)
                {
                    Console.WriteLine($"Player {player.Id + 1} has disconnected ({e.DisconnectReason})");
                    player.Client = null;
                    break;
                }
            }
        }

        private void OnClientConnected(object sender, ClientConnectedEventArgs e)
        {
            // Add client to slot
            foreach(var player in _players)
            {
                if (player.Client == null)
                {
                    player.Client = e.Client;
                    Console.WriteLine($"Player {player.Id + 1} has joined the game");
                    player.SendClientInfo();
                    break;
                }
            }
        }

        private void PromptCurrentPlayer() => _players[TurnIndex].PromptTurn();

        private void OnPlayingCard(object sender, PlayerPlayCardEventArgs e)
        {
            if (TurnIndex != e.Player.Id)
            {
                e.Cancel = true;
                return;
            }

            // ID of round winner
            int winningId = LeadingPlayerId;

            // Round of play
            int playRound = Round;

            // Put card on table
            _roundCards[e.Player.Id] = e.Card;

            if (_roundCards.All(c => c != null))
            {
                // Suit of leading player's card
                var leadingSuit = _roundCards[LeadingPlayerId].Value.Suit;
                // Highest card rank found in round pool
                var highestRank = _roundCards[LeadingPlayerId].Value.Rank;

                // Find winning card
                for (int i = 0; i < _roundCards.Length; i++)
                {
                    if (_roundCards[i].Value.Suit == leadingSuit && _roundCards[i].Value.Rank > highestRank)
                    {
                        winningId = i;
                        highestRank = _roundCards[i].Value.Rank;
                    }
                }

                // Update score for round winner
                int winningScore = ++_players[winningId].Score;

                // Empty pool
                for (int i = 0; i < _roundCards.Length; i++) _roundCards[i] = null;

                Console.WriteLine($"Player {winningId + 1} won round {Round}");

                // Create player_score packet with updated score
                var msgPlayerScore = new
                {
                    msg_type = "player_score",
                    player_index = winningId,
                    score = winningScore
                };

                // Send score update to all players
                Server.SendAll(msgPlayerScore);

                Round++;
                LeadingPlayerId = winningId;
                TurnIndex = LeadingPlayerId;                
            }
            else
            {
                NextTurn();
            }

            // Construct client_move packet
            var msgPlayCard = new
            {
                msg_type = "client_move",
                player_index = e.Player.Id,
                card = e.Card.GetCardCode(),
                round = playRound,
                next_round = Round,
                next_turn = TurnIndex,
                result_type = winningId
            };

            // Send client_move to all players
            Server.SendAll(msgPlayCard);
            
            PromptCurrentPlayer();
        }

        private void NextTurn() => TurnIndex = (TurnIndex + 1) % MaxPlayers;
    }
}

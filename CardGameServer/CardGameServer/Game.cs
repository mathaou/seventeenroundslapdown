using CardGameServer.Cards;
using System;
using System.Collections.Generic;
using System.Linq;

namespace CardGameServer
{
    public sealed class Game
    {
        private readonly GameServer _server;
        private readonly List<Player> _players;
        private bool _active = false;
        private readonly Random _rng;
        private readonly Card?[] _pool;

        public Game(int playerCount) //initiate game server and list of players
        {
            _rng = new Random();
            _server = new GameServer();
            _players = new List<Player>();
            _pool = new Card?[playerCount];
            MaxPlayers = playerCount;
            for(int i = 0; i < playerCount; i++)
            {
                var p = new Player(this, i);
                p.PlayingCard += OnPlayingCard;
                _players.Add(p);
            }
        }

        public int MaxPlayers { get; }

        public int Round { get; set; }

        public int TurnIndex { get; set; }

        public int LeadingPlayerId { get; set; }

        public Random RNG => _rng;

        public CardSuit? LeadingSuit => _pool[LeadingPlayerId]?.Suit;

        public void Start() //start server
        {
            if (_active) return;

            Console.WriteLine("Starting game...");

            if (!_server.Start()) return;
            
            _server.ClientConnected += OnClientConnected;
            _server.ClientDisconnected += OnClientDisconnected;

            NewGame();

            Console.WriteLine("Game ready");

            _active = true;
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
            }

            Round = 0;
            TurnIndex = 0;
            LeadingPlayerId = 0;
            PromptCurrentPlayer();
        }
        

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            foreach (var player in _players)
            {
                if (player.Client == e.Client)
                {
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
                    player.SendClientInfo();
                    break;
                }
            }
        }

        private void PromptCurrentPlayer()
        {
            _players[TurnIndex].PromptTurn();
        }

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

            _pool[e.Player.Id] = e.Card;

            if (_pool.All(c => c != null))
            {
                // Suit of leading player's card
                var leadingSuit = _pool[LeadingPlayerId].Value.Suit;
                // Highest card rank found in round pool
                var highestRank = _pool[LeadingPlayerId].Value.Rank;

                // Find winning card
                for (int i = 0; i < _pool.Length; i++)
                {
                    if (_pool[i].Value.Suit == leadingSuit && _pool[i].Value.Rank > highestRank)
                    {
                        winningId = i;
                        highestRank = _pool[i].Value.Rank;
                    }
                }

                // Update score for round winner
                _players[winningId].Score++;

                // Empty pool
                for (int i = 0; i < _pool.Length; i++)
                {
                    _pool[i] = null;
                }

                Console.WriteLine($"Player {winningId + 1} won round {Round}");

                Round++;
                LeadingPlayerId = winningId;
                TurnIndex = LeadingPlayerId;
                PromptCurrentPlayer();
            }
            else
            {
                NextTurn();
            }

            // Notify players of play and result
            var msgPlayCard = new
            {
                msg_type = "client_play_card",
                player_index = e.Player.Id,
                card = e.Card.GetCardCode(),
                round = playRound,
                result_type = winningId
            };

            _server.SendAll(msgPlayCard);
        }

        private void NextTurn()
        {
            TurnIndex = (TurnIndex + 1) % MaxPlayers;
            PromptCurrentPlayer();
        }
    }
}

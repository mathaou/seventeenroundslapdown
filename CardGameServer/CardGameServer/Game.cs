using CardGameServer.Cards;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace CardGameServer
{
    public sealed class Game
    {
        private readonly List<Player> _players;
        private bool _active = false;
        private readonly Random _rng;
        private readonly Card?[] _roundCards;
        private const int RoundCount = 17;

        public Game(GameSettings settings) //initiate game server and list of players
        {
            Settings = settings;
            _rng = new Random();
            Server = new GameServer(this);
            _players = new List<Player>();
            _roundCards = new Card?[settings.MaxPlayers];
            for (int i = 0; i < MaxPlayers; i++)
            {
                var p = new Player(this, i);
                p.PlayingCard += OnPlayingCard;
                _players.Add(p);
            }
        }

        public int MaxPlayers => Settings.MaxPlayers;

        public bool GameOver { get; private set; }

        public GameServer Server { get; }

        public int Round { get; set; } = 1;

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
            var deck = Card.GeneratePile(_rng, Settings.NumDecks);
            int handSize = RoundCount;

            Console.WriteLine("==== NEW GAME ====\n");

            // Set up players and deal cards
            foreach (var player in _players)
            {
                player.IsReady = false;
                player.ScoreWins = 0;
                player.ScorePoints = 0;
                player.ClearHand();
                player.AddToHand(deck.GetRange(handSize * player.Id, handSize));
                // Print hand
                Console.Write($"{player} <- ");
                player.PrintHand(-1);
                Console.Write("\n\n");
            }

            var state = GetGameState();

            foreach (var player in _players)
            {
                player.SendClientInfo();
                player.Client?.Send(state);
            }

            GameOver = false;
            Round = 1;
            TurnIndex = 0;
            LeadingPlayerId = 0;
            OnRoundStart();
            if (Server.ConnectedPlayerCount > 0 || Settings.AutoPlayFirstMove) PromptCurrentPlayer();
        }


        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            foreach (var player in _players)
            {
                if (player.Client == e.Client)
                {
                    Console.WriteLine($"{player} has disconnected ({e.DisconnectReason})");
                    player.Client = null;
                    break;
                }
            }
        }

        private void OnClientConnected(object sender, ClientConnectedEventArgs e)
        {
            // Add client to slot
            foreach (var player in _players)
            {
                if (player.Client == null)
                {
                    player.Client = e.Client;
                    Console.WriteLine($"{player} connected");
                    player.SendClientInfo();
                    player.Client.Send(GetGameState());
                    break;
                }
            }
            PromptCurrentPlayer();
        }

        public object GetGameState() => new
        {
            msg_type = "game_state",
            round = Round,
            game_state = GameOver ? "ended" : "playing",
            players = _players.ToDictionary(
                p => p.Id.ToString(),
                p => new
                {
                    name = $"Player {p.Id}",
                    hand_size = p.HandCount,
                    points = p.ScoreWins
                }),
            turn = TurnIndex,
            table = _roundCards.Select(c => c == null ? 0 : c.Value.GetCardCode()).ToArray()
        };

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

            bool slapdown = false;

            // Put card on table
            _roundCards[e.Player.Id] = e.Card;

            if (_roundCards.All(c => c != null))
            {
                // Suit of leading player's card
                var leadingSuit = _roundCards[LeadingPlayerId].Value.Suit;
                // Highest card rank found in round pool
                var highestSuitRank = _roundCards[LeadingPlayerId].Value.Rank;
                var lowestAnyRank = highestSuitRank;
                // Number of valid cards played in round
                int numValidCards = 0;

                // Find winning card
                for (int i = 0; i < _roundCards.Length; i++)
                {
                    var card = _roundCards[i].Value;
                    // If the play matches the suit, count as a valid play
                    if (card.Suit == leadingSuit)
                    {
                        numValidCards++;
                        if (card.Rank > highestSuitRank)
                        {
                            winningId = i;
                            highestSuitRank = card.Rank;
                        }
                    }

                    // Record max overall rank
                    if (card.Rank < lowestAnyRank)
                    {
                        lowestAnyRank = card.Rank;
                    }
                }
                var roundWinner = _players[winningId];

                // Empty pool
                for (int i = 0; i < _roundCards.Length; i++) _roundCards[i] = null;

                // Update score for round winner
                int winningScore = ++roundWinner.ScoreWins;
                roundWinner.ScorePoints += (int)highestSuitRank;

                // Award slapdown bonus if nobody matched leading suit and the leading card has the lowest rank
                if (slapdown = numValidCards == 1 && highestSuitRank == lowestAnyRank)
                {
                    roundWinner.ScorePoints += Settings.SlapdownBonus;
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine($"  Slapdown Bonus +{Settings.SlapdownBonus}");
                    Console.ResetColor();
                }

                Console.WriteLine($"{_players[winningId]} wins round {Round}");

                // Create player_score packet with updated score
                var msgPlayerScore = new
                {
                    msg_type = "player_score",
                    player_index = winningId,
                    wins = roundWinner.ScoreWins,
                    points = roundWinner.ScorePoints
                };

                // Send score update to all players
                Server.SendAll(msgPlayerScore);

                if (Round < RoundCount)
                {
                    Console.WriteLine($"Scores: {_players.OrderByDescending(p => p.ScoreWins).Select(p => $"{p} ({p.ScoreWins})").Aggregate((c, n) => $"{c}, {n}")}\n");
                    Round++;
                    LeadingPlayerId = winningId;
                    TurnIndex = LeadingPlayerId;
                    OnRoundStart();
                }
                else
                {
                    LeadingPlayerId = 0;
                    TurnIndex = -1;
                    GameOver = true;
                }
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
                result_type = winningId,
                slapdown = slapdown
            };

            // Send client_move to all players
            Server.SendAll(msgPlayCard);

            if (GameOver)
            {
                OnGameEnd();
            }
            else
            {
                PromptCurrentPlayer();
            }
        }

        private object CreateGameEndPacket(int winners)
        {
            return new
            {
                msg_type = "game_end",
                winner_count = winners,
                scoreboard = _players
                .OrderByDescending(p => p.ScoreWins)
                .ThenByDescending(p => p.ScorePoints)
                .Select(p => new
                {
                    id = p.Id,
                    wins = p.ScoreWins,
                    points = p.ScorePoints
                }).ToArray(),
                delay_ms = Settings.NewGameDelay
            };
        }

        private async void OnGameEnd()
        {
            var scoreboard = _players.OrderByDescending(p => p.ScoreWins).ThenByDescending(p => p.ScorePoints).ToArray();
            int winners = 1;
            var winner = scoreboard[0];
            for (int i = 1; i < scoreboard.Length; i++)
            {
                var p = scoreboard[i];
                if (p.ScoreWins < winner.ScoreWins) break;
                if (p.ScorePoints < winner.ScorePoints) break;
                winners++;
            }

            var msgGameEnd = CreateGameEndPacket(winners);

            // Send game_end message to players
            foreach (var player in _players)
            {
                player.Client?.Send(msgGameEnd);
            }

            Console.WriteLine();
            Console.WriteLine("==== GAME OVER ====\n");

            if (winners == 1)
            {
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"{winner} wins!");
            }
            else
            {
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine($"Game ends in {winners}-way tie!");
            }
            Console.ResetColor();
            Console.WriteLine();

            Console.WriteLine("Final Scores:");
            for (int i = 0; i < scoreboard.Length; i++)
            {
                if (i < winners) Console.ForegroundColor = ConsoleColor.Yellow;
                var p = scoreboard[i];
                Console.WriteLine($"  - P{p.Id + 1}: {p.ScoreWins,8} wins{p.ScorePoints,8} pts");
                Console.ResetColor();
            }
            Console.WriteLine();
            Console.WriteLine($"Starting new round in {Settings.NewGameDelay / 1000}s...");
            var voteTask = WaitForPlayerVotes();
            await await Task.WhenAny(voteTask, Task.Delay(Settings.NewGameDelay).ContinueWith(_ => true));
            Console.WriteLine();
            NewGame();
        }

        private async Task<bool> WaitForPlayerVotes()
        {
            bool votesIn = false;

            // Keep waiting until all players have voted
            while (GameOver && (Server.ConnectedPlayerCount == 0 || !(votesIn = _players.Where(p => !p.IsAutonomous).All(p => p.IsReady))))
            {
                await Task.Delay(100);
            }

            if (votesIn) Console.WriteLine("Votes are in, starting new game");

            return true;
        }

        private void OnRoundStart()
        {
            Console.WriteLine($"Round {Round} -- {_players[LeadingPlayerId]} leads");
        }

        private void NextTurn() => TurnIndex = (TurnIndex + 1) % MaxPlayers;
    }
}

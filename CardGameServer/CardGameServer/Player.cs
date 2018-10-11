using CardGameServer.Cards;
using CardGameServer.Messages;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace CardGameServer
{
    public sealed class Player
    {
        private const int InitialHandListCapacity = 17;

        private readonly Game _game;
        private PlayerClient _client;
        private List<Card> _hand;
        private readonly Random _rng;

        public Player(Game game, int id)
        {
            _game = game;
            Id = id;
            _hand = new List<Card>(InitialHandListCapacity);
            _rng = game.RNG;
        }

        // ID of player for network use
        public int Id { get; }

        // Number of rounds won
        public int ScoreWins { get; set; }

        // Number of points gained
        public int ScorePoints { get; set; }

        // Is ready for next game
        public bool IsReady { get; set; }

        // Get current hand
        public IEnumerable<Card> GetCards()
        {
            foreach (var card in _hand) yield return card;
        }

        public event EventHandler<PlayerPlayCardEventArgs> PlayingCard;

        // Network client for player
        public PlayerClient Client
        {
            get => _client;
            set
            {
                if (_client != null)
                {
                    _client.Disconnected -= OnClientDisconnected;
                    _client.MessageReceived -= OnClientMessageReceived;
                }

                _client = value;

                if (value != null)
                {
                    _client.Disconnected += OnClientDisconnected;
                    _client.MessageReceived += OnClientMessageReceived;
                }
            }
        }

        private void OnClientMessageReceived(object sender, ClientMessageEventArgs e)
        {
            ClientMessage.Run(_game, this, e);
        }

        public bool IsAutonomous => _client == null;

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            Client = null;
            Console.WriteLine($"{this} has disconnected ({e.DisconnectReason})");
        }

        public async void PromptTurn()
        {
            // Where player is autonomous, have them automatically select and play a card
            if (Client == null && _game.Settings.AutoPlayEnabled)
            {
                if (!_game.Settings.AutoPlayAllBotsEnabled && _game.Server.ConnectedPlayerCount == 0) return;

                await Task.Delay(_game.Settings.AutoPlayDelay);
                if (_game.LeadingPlayerId == Id || _game.LeadingSuit == null)
                {
                    PlayCard(_rng.Next(_hand.Count));
                }
                else
                {
                    var matchingSuitCards = _hand.Select((c, i) => (c, i))
                        .Where(t => t.c.Suit == _game.LeadingSuit).ToArray();
                    int minIndex = 0, maxIndex = 0;
                    CardRank minRank = CardRank.Two, maxRank = CardRank.Two;
                    for (int i = 0; i < matchingSuitCards.Length; i++)
                    {
                        Card c = matchingSuitCards[i].c;
                        if (c.Rank < minRank)
                        {
                            minIndex = i;
                            minRank = c.Rank;
                        }
                        if (c.Rank > maxRank)
                        {
                            maxIndex = i;
                            maxRank = c.Rank;
                        }
                    }

                    switch (_game.Settings.Difficulty)
                    {
                        case 1:
                            PlayCard(matchingSuitCards.Length > 0 ? matchingSuitCards[minIndex].i : _rng.Next(_hand.Count));
                            break;
                        case 2:
                        default:
                            PlayCard(matchingSuitCards.Length > 0 ? matchingSuitCards[_rng.Next(matchingSuitCards.Length)].i : _rng.Next(_hand.Count));
                            break;
                        case 3:
                            PlayCard(matchingSuitCards.Length > 0 ? matchingSuitCards[maxIndex].i : _rng.Next(_hand.Count));
                            break;
                    }

                }
            }
        }

        public bool PlayCard(int cardIndex)
        {
            if (cardIndex < 0 || cardIndex >= HandCount || _game.TurnIndex != Id) return false;
            var card = _hand[cardIndex];
            var e = new PlayerPlayCardEventArgs(this, card);
            if (!IsAutonomous) Console.ForegroundColor = ConsoleColor.Green;
            Console.Write($"P{Id + 1}");
            Console.ResetColor();
            Console.Write($": ");
            PrintHand(cardIndex);
            Console.Write($" -> ");
            card.Print();
            Console.WriteLine();
            PlayingCard?.Invoke(this, e);
            if (!e.Cancel)
            {
                _hand.RemoveAt(cardIndex);
                SendClientInfo();
                return true;
            }
            return false;
        }

        public void SendClientInfo()
        {
            if (Client == null) return;
            var obj = new JObject
            {
                ["msg_type"] = "client_info",
                ["player_id"] = Id,
                ["cards"] = new JArray(_hand.Select(c => (int)c.GetCardCode()).ToArray())
            };
            Client?.Send(obj);
        }

        public void ClearHand() => _hand.Clear();

        public Card GetCard(int i) => _hand[i];

        public void AddToHand(IEnumerable<Card> cards) => _hand.AddRange(cards);

        public int HandCount => _hand.Count;

        public void PrintHand(int highlight = -1)
        {
            for (int i = 0; i < HandCount; i++)
            {
                GetCard(i).Print(i % 2 == 0, i == highlight);
            }
            Console.ResetColor();            
        }

        public void Reset()
        {
            IsReady = false;
            ScoreWins = 0;
            ScorePoints = 0;
            ClearHand();
        }

        public override string ToString() => $"P{Id + 1}";
    }
}

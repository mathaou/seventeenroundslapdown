using CardGameServer.Cards;
using CardGameServer.Messages;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
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

        public int Id { get; }

        public int Score { get; set; }

        public event EventHandler<PlayerPlayCardEventArgs> PlayingCard;

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
        }

        public async void PromptTurn()
        {
            // Where player is autonomous, have them automatically select and play a card
            if (Client == null)
            {
                await Task.Delay(1500);
                if (_game.LeadingPlayerId == Id || _game.LeadingSuit == null)
                {
                    PlayCard(_rng.Next(_hand.Count));
                }
                else
                {
                    var matchingSuitCards = _hand.Select((c, i) => (c, i)).Where(t => t.c.Suit == _game.LeadingSuit).ToArray();
                    PlayCard(matchingSuitCards.Length > 0 ? matchingSuitCards[_rng.Next(matchingSuitCards.Length)].i : _rng.Next(_hand.Count));
                }
            }
        }

        public void PlayCard(int cardIndex)
        {
            var card = _hand[cardIndex];
            var e = new PlayerPlayCardEventArgs(this, card);
            Console.WriteLine($"Player {Id + 1} playing {card} (i = {cardIndex})");
            PlayingCard?.Invoke(this, e);
            if (!e.Cancel)
            {
                _hand.RemoveAt(cardIndex);
            }
        }

        public void SendClientInfo()
        {
            if (Client == null) return;
            var obj = new JObject
            {
                ["msg_type"] = "client_info",
                ["player_id"] = Id,
                ["cards"] = new JArray(_hand.Select(c => c.GetCardCode()).ToArray())
            };
            Client?.Send(obj);
        }

        public void ClearHand() => _hand.Clear();

        public void AddToHand(IEnumerable<Card> cards) => _hand.AddRange(cards);

        public int HandCount => _hand.Count;
    }
}

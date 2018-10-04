using CardGameServer.Cards;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer
{
    public sealed class Game
    {
        private readonly GameServer _server;
        private readonly List<Player> _players;
        private bool _active = false;
        private readonly Random _rng;

        public Game(int playerCount) //initiate game server and list of players
        {
            _rng = new Random();
            _server = new GameServer();
            _players = new List<Player>();
            MaxPlayers = playerCount;
            for(int i = 0; i < playerCount; i++)
            {
                _players.Add(new Player(this, i));
            }
        }

        public int MaxPlayers { get; }

        public int Round { get; set; }

        public int TurnIndex { get; set; }

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
        }

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            foreach (var player in _players)
            {
                if (player.Client == e.Client)
                {
                    player.Client = null;
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
                }
            }
        }
    }
}

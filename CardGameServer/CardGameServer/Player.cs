﻿using CardGameServer.Cards;
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

        public Player(Game game, int id)
        {
            _game = game;
            Id = id;
            _hand = new List<Card>(InitialHandListCapacity);
        }

        public int Id { get; }

        public PlayerClient Client
        {
            get => _client;
            set
            {
                if (_client != null)
                {
                    _client.Disconnected -= OnClientDisconnected;
                }

                if (value != null)
                {
                    _client.Disconnected += OnClientDisconnected;
                }
                _client = value;
            }
        }

        public bool IsAutonomous => _client == null;

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            Client = null;
        }

        public int HandCount => _hand.Count;
    }
}
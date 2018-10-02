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

        public Game() //initiate game server and list of players
        {
            _server = new GameServer();
            _players = new List<Player>();
        }

        public void Start() //start server
        {
            if (_active) return;

            Console.WriteLine("Starting game...");

            if (!_server.Start()) return;
            
            _server.ClientConnected += OnClientConnected;

            Console.WriteLine("Game ready");

            _active = true;
        }

        private void OnClientConnected(object sender, ClientConnectedEventArgs e)
        {
            // TODO: Add new client to first client-less player slot
        }
    }
}

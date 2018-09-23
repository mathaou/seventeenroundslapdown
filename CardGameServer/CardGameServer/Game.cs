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

        public Game()
        {
            _server = new GameServer();
            _players = new List<Player>();
        }

        public void Start()
        {
            if (_active) return;

            Console.WriteLine("Starting game...");

            _server.Start();
            

            Console.WriteLine("Game ready");

            _active = true;
        }

    }
}

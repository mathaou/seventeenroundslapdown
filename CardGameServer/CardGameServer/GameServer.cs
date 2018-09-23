using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer
{
    public sealed class GameServer
    {
        private const int ServerPort = 6789;

        private readonly TcpListener _server;

        private bool _active;

        public event EventHandler<ClientConnectedEventArgs> ClientConnected;

        public GameServer()
        {
            _server = new TcpListener(IPAddress.Any, ServerPort);            
        }

        public void Start()
        {
            if (_active) return;

            try
            {
                Console.WriteLine("Starting server...");
                _server.Start();
                AcceptClientAsync();
                _active = true;
                Console.WriteLine("Server active");
            }
            catch (Exception ex)
            {
            }
        }

        private async void AcceptClientAsync()
        {
            try
            {
                var client = await _server.AcceptTcpClientAsync();
                var playerClient = new PlayerClient(client);
                ClientConnected?.Invoke(this, new ClientConnectedEventArgs(playerClient));
            }
            catch (Exception ex)
            {

            }

            AcceptClientAsync();
        }
    }
}

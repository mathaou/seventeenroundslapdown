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
        private const int ServerPort = 6789; //always 6789

        private readonly TcpListener _server;

        private bool _active;

        public event EventHandler<ClientConnectedEventArgs> ClientConnected;
        public event EventHandler<ClientDisconnectedEventArgs> ClientDisconnected;

        public GameServer()
        {
            _server = new TcpListener(IPAddress.Any, ServerPort);            
        }

        public bool Start()
        {
            if (_active) return false;

            try
            {
                Console.WriteLine("Starting server...");
                _server.Start();
                AcceptClientAsync();
                _active = true;
                Console.WriteLine("Server active");
                return true;
            }
            catch (Exception ex)
            {
                // TODO: Handle failed start
                System.Console.WriteLine($"Exception occured on server start:\n{ex}");
                return false;
            }
        }

        private async void AcceptClientAsync()
        {
            try
            {
                var client = await _server.AcceptTcpClientAsync();
                var playerClient = new PlayerClient(client);
                playerClient.Disconnected += OnClientDisconnected;
                ClientConnected?.Invoke(this, new ClientConnectedEventArgs(playerClient));
                playerClient.Start();
                Console.WriteLine($"Player {playerClient} has joined the game");
            }
            catch (Exception ex)
            {
                System.Console.WriteLine($"Exception occured on client sync:\n{ex}");
            }

            AcceptClientAsync();
        }

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            Console.WriteLine($"Player {e.Client} has disconnected ({e.DisconnectReason})");
            ClientDisconnected?.Invoke(this, e);
        }
    }
}

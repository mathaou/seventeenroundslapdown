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
                // TODO: Handle failed startl
                System.Console.WriteLine($"Exception occured on server start:\n{ex}");
                // Interpolated strings will automatically call ToString on objects inserted in the string
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
            }
            catch (Exception ex)
            {
                System.Console.WriteLine($"Exception occured on client sync:\n{ex}");
            }

            AcceptClientAsync();
        }

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            
        }
    }
}

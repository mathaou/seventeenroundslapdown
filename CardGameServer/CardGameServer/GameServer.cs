using CardGameServer.Messages;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;

namespace CardGameServer
{
    public sealed class GameServer
    {
        private const int ServerPort = 6789; //always 6789

        private readonly TcpListener _server;
        private readonly HashSet<PlayerClient> _clients;

        private bool _active;

        public event EventHandler<ClientConnectedEventArgs> ClientConnected;
        public event EventHandler<ClientDisconnectedEventArgs> ClientDisconnected;

        public GameServer()
        {
            ClientMessage.Init();
            _server = new TcpListener(IPAddress.Any, ServerPort);
            _clients = new HashSet<PlayerClient>();
        }

        public int ConnectedPlayerCount => _clients.Count;

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
                Console.WriteLine($"Exception occured on server start:\n{ex}");
                return false;
            }
        }

        public void SendAll(JObject obj)
        {
            foreach(var p in _clients)
            {
                p.Send(obj);
            }
        }

        public void SendAll(object obj)
        {
            var json = JObject.FromObject(obj);
            foreach (var p in _clients)
            {
                p.Send(json);
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
                _clients.Add(playerClient);
                Console.WriteLine($"Player {playerClient} has joined the game");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Exception occured on client sync:\n{ex}");
            }

            AcceptClientAsync();
        }

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            Console.WriteLine($"Player {e.Client} has disconnected ({e.DisconnectReason})");
            _clients.Remove(e.Client);
            ClientDisconnected?.Invoke(this, e);
        }
    }
}

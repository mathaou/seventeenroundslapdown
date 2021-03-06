﻿/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

using CardGameServer.Messages;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace CardGameServer
{
    public sealed class GameServer
    {
        private const int ServerPort = 6789;

        private readonly TcpListener _server;
        private readonly HashSet<PlayerClient> _clients;
        private readonly Thread _acceptClientsThread;
        private readonly object _startStopSync = new object();
        private readonly Game _game;
        private bool _active;

        public event EventHandler<ClientConnectedEventArgs> ClientConnected;
        public event EventHandler<ClientDisconnectedEventArgs> ClientDisconnected;

        public GameServer(Game g)
        {
            _game = g;
            ClientMessage.Init();
            _server = new TcpListener(IPAddress.Any, ServerPort);
            _clients = new HashSet<PlayerClient>();
            _acceptClientsThread = new Thread(AcceptClients);
        }

        public int ConnectedPlayerCount => _clients.Count;

        public bool Start()
        {
            lock(_startStopSync)
            {
                if (_active) return false;

                try
                {
                    Console.WriteLine("Starting server...");
                    _server.Start();
                    _acceptClientsThread.Start();
                    _active = true;
                    Console.WriteLine("Server active");
                    return true;
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Exception occured on server start:\n{ex}");
                    return false;
                }
            }
        }

        public bool Stop()
        {
            lock(_startStopSync)
            {
                if (!_active) return false;

                try
                {
                    _active = false;
                    _server.Stop();
                    _acceptClientsThread.Join();

                }
                catch (Exception ex)
                {
                    Console.WriteLine($"An error occurred while stopping the server: {ex}");
                }

                return true;
            }
        }

        public void SendAll(JObject obj)
        {
            foreach (var p in _clients)
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

        private void AcceptClients()
        {
            Console.WriteLine("Listening for connections");
            while (_active)
            {
                try
                {
                    // Wait for new connection
                    var client = _server.AcceptTcpClient();

                    // Create PlayerClient wrapper for new client connection
                    var playerClient = new PlayerClient(client);

                    // Reject player if they cannot be placed in the game
                    if (ConnectedPlayerCount >= _game.MaxPlayers)
                    {
                        var msgReject = new
                        {
                            msg_type = "client_reject",
                            reject_reason = "server_full"
                        };
                        playerClient.Send(msgReject);
                        playerClient.Stop();
                        continue;
                    }

                    playerClient.Disconnected += OnClientDisconnected;
                    ClientConnected?.Invoke(this, new ClientConnectedEventArgs(playerClient));

                    // Start up listener for client
                    playerClient.Start();

                    // Add client to active client list
                    _clients.Add(playerClient);
                }
                catch (SocketException ex)
                {
                    // Handle socket error from canceling AcceptTcpClient call
                    if (ex.SocketErrorCode == SocketError.Interrupted)
                    {
                        return;
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Exception occured on client sync:\n{ex}");
                }
            }
        }

        private void OnClientDisconnected(object sender, ClientDisconnectedEventArgs e)
        {
            _clients.Remove(e.Client);
            ClientDisconnected?.Invoke(this, e);
        }
    }
}

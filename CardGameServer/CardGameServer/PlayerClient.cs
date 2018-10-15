/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace CardGameServer
{
    public sealed class PlayerClient
    {
        private const string DefaultDisconnectReason = "Client disconnected";
        private const int BufferSize = 4096;

        private readonly byte[] _receiveBuffer;
        private bool _active = false;
        private readonly NetworkStream _stream;
        private readonly string _clientId;
        private string _disconnectReason = DefaultDisconnectReason;
        private readonly Thread _listenThread;

        public PlayerClient(TcpClient client)
        {
            Client = client;
            _stream = client.GetStream();
            _clientId = (client.Client.RemoteEndPoint as IPEndPoint).Address.ToString();
            _receiveBuffer = new byte[BufferSize];
            _listenThread = new Thread(Listen);
        }

        public TcpClient Client { get; }

        public event EventHandler<ClientDisconnectedEventArgs> Disconnected;

        public event EventHandler<ClientMessageEventArgs> MessageReceived;

        public void Start()
        {
            if (_active) return;

            _active = true;

            _listenThread.Start();
        }

        public void Stop()
        {
            if (!_active) return;

            _active = false;

            try
            {
                Client.Close();
                _listenThread.Join();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Exception occurred while closing client.\n\n{ex}");
            }
        }

        public async void Send(JObject obj)
        {   
            if (Client == null || obj == null) return;
            var jsonString = obj.ToString();
            var jsonStringBytes = Encoding.UTF8.GetBytes(jsonString);

            await _stream.WriteAsync(jsonStringBytes, 0, jsonStringBytes.Length);
        }

        public void Send(object obj) => Send(JObject.FromObject(obj));

        public override string ToString() => _clientId;

        private void Listen()
        {
            while(_active)
            {
                try
                {
                    bool success = Client.Client.Poll(0, SelectMode.SelectRead);

                    int receiveSize = Client.Client.Receive(_receiveBuffer, SocketFlags.None);

                    if (receiveSize <= 0)
                    {
                        // Client has disconnected.
                        _active = false;
                        return;
                    }

                    try
                    {
                        using (var reader = new StreamReader(new MemoryStream(_receiveBuffer, 0, receiveSize), Encoding.UTF8))
                        using (var jsonReader = new JsonTextReader(reader))
                        {
                            while (!reader.EndOfStream)
                            {
                                var obj = JToken.ReadFrom(jsonReader);

                                var msgType = obj["msg_type"] as JValue;

                                if (msgType != null)
                                {
                                    // Send packet data off to correct interpreter
                                    MessageReceived?.Invoke(this, new ClientMessageEventArgs(msgType.Value.ToString(), obj as JObject));
                                }
                            }
                        }
                    }
                    catch (JsonReaderException ex)
                    {
                        Console.WriteLine($"Received bad JSON from {_clientId} ({ex.Message})");
                    }
                }
                catch (SocketException ex)
                {
                    // Log socket errors

                    switch (ex.SocketErrorCode)
                    {
                        case SocketError.TimedOut:
                            _disconnectReason = "Timed out";
                            break;
                        case SocketError.ConnectionReset:
                            _disconnectReason = "Connection reset";
                            break;
                        default:
                            Console.WriteLine($"Socket error in PlayerClient.Listen():\n{ex}");
                            break;
                    }
                    _active = false;
                }
                catch (Exception ex)
                {
                    // Log misc errors
                    Console.WriteLine($"Exception occurred in PlayerClient.Listen():\n{ex}");
                }
                finally
                {
                    if (!Client.Connected)
                    {
                        _active = false;
                    }

                    if (!_active)                    
                    {
                        Disconnected?.Invoke(this, new ClientDisconnectedEventArgs(this, _disconnectReason));
                    }
                }
            }            
        }
    }
}

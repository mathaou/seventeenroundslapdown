using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer
{
    public sealed class PlayerClient
    {
        private const string DefaultDisconnectReason = "Client disconnected";
        private const int ReceiveBufferSize = 4096;

        private readonly byte[] _receiveBuffer;
        private bool _active = false;
        private readonly NetworkStream _stream;
        private string _disconnectReason = DefaultDisconnectReason;

        public PlayerClient(TcpClient client)
        {
            Client = client;
            _stream = client.GetStream();
            _receiveBuffer = new byte[ReceiveBufferSize];
        }

        public TcpClient Client { get; }

        public event EventHandler<ClientDisconnectedEventArgs> Disconnected;

        public void Start()
        {
            if (_active) return;

            _active = true; 

            Listen();
        }

        private async void Listen()
        {
            try
            {   
                bool success = await Task.Run(() => Client.Client.Poll(0, SelectMode.SelectRead));

                int receiveSize = Client.Client.Receive(_receiveBuffer, SocketFlags.Peek);
                
                if (receiveSize <= 0)
                {
                    // Client has disconnected.
                    _active = false;                    
                    return;
                }

                using (var reader = new StreamReader(new MemoryStream(_receiveBuffer, 0, receiveSize), Encoding.UTF8))
                using (var jsonReader = new JsonTextReader(reader))
                {
                    // Read JSON object from packet
                    if (jsonReader.Read() && jsonReader.TokenType == JsonToken.StartObject)
                    {
                        var obj = jsonReader.Value as JObject;

                        var msgType = obj["msg_type"];

                        if (msgType != null)
                        {
                            // Send packet data off to correct interpreter


                        }
                    }
                }
                
            }
            catch(SocketException ex)
            {
                // Log socket errors
                Console.WriteLine($"Socket error in PlayerClient.Listen():\n{ex}");
                switch(ex.SocketErrorCode)
                {
                    case SocketError.TimedOut:
                        _disconnectReason = "Timed out";
                        return;
                    case SocketError.ConnectionReset:
                        _disconnectReason = "Connection reset";
                        return;
                }
                _active = false;
            }
            catch(Exception ex)
            {
                // Log misc errors
                Console.WriteLine($"Exception occured in @PlayerClient.Listen():\n{ex}");

            }
            finally
            {
                if (!Client.Connected)
                {
                    _active = false;
                }

                if (_active)
                {
                    Listen();
                }
                else
                {
                    Disconnected?.Invoke(this, new ClientDisconnectedEventArgs(this, _disconnectReason));
                }
            }
        }
    }
}

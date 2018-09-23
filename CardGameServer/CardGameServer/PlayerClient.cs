using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer
{
    public sealed class PlayerClient
    {
        private const int ReceiveBufferSize = 4096;

        private readonly byte[] _receiveBuffer;
        private bool _active = false;
        private readonly NetworkStream _stream;

        public PlayerClient(TcpClient client)
        {
            Client = client;
            _stream = client.GetStream();
            _receiveBuffer = new byte[ReceiveBufferSize];
        }

        public TcpClient Client { get; }

        public void Start()
        {
            if (_active) return;

            Listen();

            _active = true;
        }

        private async void Listen()
        {
            try
            {
                int receiveSize = await _stream.ReadAsync(_receiveBuffer, 0, ReceiveBufferSize);

                if (receiveSize <= 0)
                {
                    // Client has disconnected.
                    // TODO: Notify server to clear client out of player slot
                    return;
                }
            }
            catch(Exception ex)
            {

            }
            finally
            {
                if (_active)
                {
                    Listen();
                }
            }
        }
    }
}

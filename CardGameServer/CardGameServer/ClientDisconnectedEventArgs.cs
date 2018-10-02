using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer
{
    public sealed class ClientDisconnectedEventArgs
    {
        public PlayerClient Client { get; }

        public string DisconnectReason { get; }

        internal ClientDisconnectedEventArgs(PlayerClient client, string reason)
        {
            Client = client;
            DisconnectReason = reason;
        }
    }
}

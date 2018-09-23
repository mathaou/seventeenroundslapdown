using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer
{
    public sealed class ClientConnectedEventArgs
    {
        public PlayerClient Client { get; }

        internal ClientConnectedEventArgs(PlayerClient client)
        {
            Client = client;
        }
    }
}

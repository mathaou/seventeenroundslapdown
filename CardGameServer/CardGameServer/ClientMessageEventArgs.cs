using Newtonsoft.Json.Linq;
using System;

namespace CardGameServer
{
    public sealed class ClientMessageEventArgs : EventArgs
    {
        public ClientMessageEventArgs(string msgType, JObject message)
        {
            MessageType = msgType;
            MessageObject = message;
        }

        public string MessageType { get; }
        public JObject MessageObject { get; }
    }
}

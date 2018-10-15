/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

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

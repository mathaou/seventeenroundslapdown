using System;

namespace CardGameServer.Messages
{
    [AttributeUsage(AttributeTargets.Class)]
    public sealed class ClientMessageTypeAttribute : Attribute
    {
        public ClientMessageTypeAttribute(string messageType)
        {
            MessageType = messageType;
        }

        public string MessageType { get; }
    }
}

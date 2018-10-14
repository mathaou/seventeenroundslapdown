/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

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

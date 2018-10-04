using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer.Messages
{
    public abstract class ClientMessage
    {
        private static readonly Dictionary<string, ClientMessage> _messageMap;

        static ClientMessage()
        {
            _messageMap = new Dictionary<string, ClientMessage>();
            foreach (var type in typeof(ClientMessage).Assembly.GetTypes().Where(t => t.IsSubclassOf(typeof(ClientMessage))))
            {
                var attrMsgType = type.GetCustomAttribute<ClientMessageTypeAttribute>();
                if (attrMsgType == null || String.IsNullOrWhiteSpace(attrMsgType.MessageType)) continue;
                _messageMap[attrMsgType.MessageType] = Activator.CreateInstance(type) as ClientMessage;
            }
        }

        [MethodImpl(MethodImplOptions.NoOptimization | MethodImplOptions.NoInlining)]
        public static void Init() { }

        public abstract void Run(Game g, Player p, JObject message);

        public static void Run(Game g, Player p, ClientMessageEventArgs e)
        {
            if (_messageMap.TryGetValue(e.MessageType, out ClientMessage msg))
            {
                msg.Run(g, p, e.MessageObject);
            }
        }
    }
}

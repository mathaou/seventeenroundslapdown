using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;

namespace CardGameServer.Messages
{
    [ClientMessageType("client_play_card")]
    public class ClientPlayCardMessage : ClientMessage
    {
        public override void Run(Game g, Player p, JObject message)
        {
            p.Client.Send(JObject.FromObject(new { msg = "Client Play Card message received" }));
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;

namespace CardGameServer.Messages
{
    [ClientMessageType("vote_new_game")]
    public class ClientVoteReadyMessage : ClientMessage
    {
        public override void Run(Game g, Player p, JObject message)
        {
            if (g.GameOver)
            {
                p.IsReady = true;
                var msgVotes = g.CreatePlayerVotesMessage();
                g.Server.SendAll(msgVotes);
                Console.WriteLine($"{p} is ready");
            }
        }
    }
}

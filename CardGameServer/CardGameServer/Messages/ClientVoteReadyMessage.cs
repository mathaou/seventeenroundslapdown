/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

using System;
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

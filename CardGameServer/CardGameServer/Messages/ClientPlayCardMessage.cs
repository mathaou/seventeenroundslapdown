/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

using CardGameServer.Cards;
using Newtonsoft.Json.Linq;

namespace CardGameServer.Messages
{
    [ClientMessageType("play_card")]
    public class ClientPlayCardMessage : ClientMessage
    {
        public override void Run(Game g, Player p, JObject message)
        {
            var card = Card.FromCardCode(message["card_id"].Value<byte>());
            int cardIndex = message["card_index"].Value<int>();

            // Make sure it's their turn
            if (g.TurnIndex != p.Id) return;
            // Make sure the card exists
            if (cardIndex < 0 || cardIndex >= p.HandCount) return;

            p.PlayCard(cardIndex);            
        }
    }
}

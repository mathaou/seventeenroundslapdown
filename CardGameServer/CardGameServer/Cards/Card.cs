using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer.Cards
{
    struct Card
    {
        public CardRank Rank { get; set; }
        public CardSuit Suit { get; set; }

        public Card(CardRank rank, CardSuit suit)
        {
            Rank = rank;
            Suit = suit;
        }

        public byte GetCardCode() => (byte)(((byte)Rank << 4) | (byte)Suit);

        public static Card FromCardCode(byte code) => new Card();
    }
}

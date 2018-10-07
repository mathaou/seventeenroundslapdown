using System;
using System.Collections.Generic;

namespace CardGameServer.Cards
{
    public struct Card
    {
        public CardRank Rank { get; set; }
        public CardSuit Suit { get; set; }

        public Card(CardRank rank, CardSuit suit)
        {
            Rank = rank;
            Suit = suit;
        }

        public byte GetCardCode() => (byte)(((byte)Rank << 4) | (byte)Suit);

        public override string ToString() => $"{Rank} of {Suit}";

        public static Card FromCardCode(byte code) => new Card((CardRank)(code >> 4), (CardSuit)(code & 0x0F));

        public static List<Card> GeneratePile(Random rng, int decks = 1)
        {
            var pile = new List<Card>(decks * 52);
            const int shuffles = 100;

            // Card generation
            for (int i = 0; i < decks; i++) // deck
            {
                for (int j = 1; j <= 4; j++) // suit
                {
                    for (int k = 2; k <= 14; k++) // rank
                    {
                        pile.Add(new Card((CardRank)k, (CardSuit)j));
                    }
                }
            }

            // Shuffling
            if (decks > 0)
            {
                for (int i = 0; i < shuffles; i++)
                {
                    for (int j = 0; j < pile.Count; j++)
                    {
                        int swapIndex = rng.Next(0, pile.Count);
                        var tempCard = pile[j];
                        pile[j] = pile[swapIndex];
                        pile[swapIndex] = tempCard;
                    }
                }
            }

            return pile;
        }
    }
}

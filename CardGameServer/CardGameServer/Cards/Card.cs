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

        public void Print(bool dark = false, bool highlight = false)
        {
            Console.BackgroundColor = highlight ? ConsoleColor.Yellow : dark ? ConsoleColor.Gray : ConsoleColor.White;
            Console.ForegroundColor = Suit == CardSuit.Hearts || Suit == CardSuit.Diamonds ? ConsoleColor.Red : ConsoleColor.Black;
            switch (Rank)
            {
                case CardRank.Two:
                case CardRank.Three:
                case CardRank.Four:
                case CardRank.Five:
                case CardRank.Six:
                case CardRank.Seven:
                case CardRank.Eight:
                case CardRank.Nine:
                    Console.Write((int)Rank);
                    break;
                case CardRank.Ten:
                    Console.Write('X');
                    break;
                case CardRank.Ace:
                    Console.Write('A');
                    break;
                case CardRank.Jack:
                    Console.Write('J');
                    break;
                case CardRank.Queen:
                    Console.Write('Q');
                    break;
                case CardRank.King:
                    Console.Write('K');
                    break;
            }

            switch (Suit)
            {
                case CardSuit.Clubs:
                    Console.Write('\u2663');
                    break;
                case CardSuit.Diamonds:
                    Console.Write('\u2666');
                    break;
                case CardSuit.Hearts:
                    Console.Write('\u2665');
                    break;
                case CardSuit.Spades:
                    Console.Write('\u2660');
                    break;
            }

            Console.ResetColor();
        }
    }
}

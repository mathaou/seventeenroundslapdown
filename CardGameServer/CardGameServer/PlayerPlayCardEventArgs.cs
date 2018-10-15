/*
 * Nicholas Fleck, Matthew Farstad, Shane Saunders, Matthew Dill
 * CS410 - Software Engineering
 * 10/14/2018
 */

using CardGameServer.Cards;
using System;

namespace CardGameServer
{
    public sealed class PlayerPlayCardEventArgs : EventArgs
    {
        public PlayerPlayCardEventArgs(Player p, Card c)
        {
            Player = p;
            Card = c;
        }

        public Player Player { get; }
        public Card Card { get; }
        public bool Cancel { get; set; }
    }
}

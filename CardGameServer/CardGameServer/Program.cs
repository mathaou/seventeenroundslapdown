using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.Title = "17RSD Server";
            var game = new Game(3);
            game.Start();
            Console.ReadKey(true);
        }
    }
}

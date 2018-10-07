using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CardGameServer
{
    class Program
    {
        private const string SettingsFilePath = "./settings.json";

        static void Main(string[] args)
        {
            Console.Title = "17RSD Server";
            GameSettings settings;
            if (File.Exists(SettingsFilePath))
            {
                settings = GameSettings.Load(SettingsFilePath);
                Console.WriteLine("Loaded settings successfully");
            }
            else
            {
                settings = new GameSettings();
                Console.WriteLine("No settings file found -- using defaults");
            }
            var game = new Game(settings);
            game.Start();
            Console.CancelKeyPress += (sender, e) =>
            {
                game.Stop();
                Environment.Exit(0);
            };
            Console.ReadKey(true);
            game.Stop();
        }
    }
}

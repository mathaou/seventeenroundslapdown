using Newtonsoft.Json;
using System.ComponentModel;
using System.IO;

namespace CardGameServer
{
    [JsonObject(MemberSerialization = MemberSerialization.OptIn)]
    public sealed class GameSettings
    {
        [JsonProperty("auto_play_delay_ms", DefaultValueHandling = DefaultValueHandling.Populate)]
        [DefaultValue(1500)]
        public int AutoPlayDelay { get; set; } = 1500;

        [JsonProperty("max_players", DefaultValueHandling = DefaultValueHandling.Populate)]
        [DefaultValue(3)]
        public int MaxPlayers { get; set; } = 3;

        [JsonProperty("num_decks", DefaultValueHandling = DefaultValueHandling.Populate)]
        [DefaultValue(1)]
        public int NumDecks { get; set; } = 1;

        [JsonProperty("new_game_delay_ms", DefaultValueHandling = DefaultValueHandling.Populate)]
        [DefaultValue(10000)]
        public int NewGameDelay { get; set; } = 10000;

        [JsonProperty("auto_play_enabled", DefaultValueHandling = DefaultValueHandling.Populate)]
        [DefaultValue(true)]
        public bool AutoPlayEnabled { get; set; } = true;

        [JsonProperty("auto_play_all_bots", DefaultValueHandling = DefaultValueHandling.Populate)]
        [DefaultValue(true)]
        public bool AutoPlayAllBotsEnabled { get; set; } = true;

        [JsonProperty("auto_play_first_move", DefaultValueHandling = DefaultValueHandling.Populate)]
        [DefaultValue(false)]
        public bool AutoPlayFirstMove { get; set; } = false;

        [JsonProperty("difficulty", DefaultValueHandling = DefaultValueHandling.Populate)]
        [DefaultValue(2)]
        public int Difficulty { get; set; } = 2;

        public static GameSettings Load(string path)
        {
            return JsonConvert.DeserializeObject<GameSettings>(File.ReadAllText(path));
        }
    }
}

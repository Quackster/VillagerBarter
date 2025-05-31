# VillagerBarter

**VillagerBarter** is a Bukkit plugin that enables villagers to automatically barter with nearby dropped items based on their configured trading recipes. This plugin simulates the experience of villagers autonomously trading in a survival-like environment without needing direct player interaction.

## Features

- Villagers automatically check for nearby dropped items every 5 seconds.
- If a valid trade can be made using dropped items, the villager:
  - Plays pickup and trade sounds.
  - Faces the nearest player (if any).
  - Drops the trade result item naturally.
  - Increments the recipe usage count.
- Trades are limited to villagers of certain professions and levels (1–5).
- Supports multi-ingredient trades (up to 2 items).

## Version Support

VillagerBarter is designed for Minecraft **1.21.x** and has been tested against Spigot and Paper for this version series. While it may work on earlier or later versions, compatibility is not guaranteed. For best results, use with Minecraft 1.21.4 or later within the 1.21 series.

## Supported Professions

The following professions are allowed to perform barters:

- Armorer
- Butcher
- Cartographer
- Cleric
- Farmer
- Fisherman
- Fletcher
- Leatherworker
- Librarian
- Mason
- Shepherd
- Toolsmith
- Weaponsmith

## Configuration

There is no configuration file. All behavior is currently hardcoded, including:

- **Search radius:** 3 blocks in each direction.
- **Trade check interval:** Every 5 seconds.
- **Eligible villager level:** 1 to 5.

## How It Works

1. Every 5 seconds, the plugin scans all villagers in loaded worlds.
2. If a villager is within the allowed professions and level range, it scans for nearby dropped items.
3. If the dropped items match a valid recipe:
   - Items are consumed.
   - The villager simulates pickup behavior.
   - The trade is performed and the result is dropped.
   - The villager may face the nearest player.
4. Recipes are updated to reflect usage.

## Development

This plugin is written in Java using the Bukkit API.

## Installation

1. Build the plugin using your preferred IDE or build tool (e.g. Maven or Gradle).
2. Place the generated `.jar` file in your server's `plugins` folder.
3. Start the server. The plugin will activate automatically.

## Notes

- The plugin does not currently support configuration or permissions.
- Items set for trading are made unpickable to avoid interference.
- Trades with maxed-out usage will be skipped.

---

*Developed for use with Minecraft server mods using the Bukkit API.*

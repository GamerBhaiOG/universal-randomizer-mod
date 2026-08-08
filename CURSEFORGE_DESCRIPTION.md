# 🎲 Universal Randomizer

[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge Supported](https://img.shields.io/badge/Loader-Forge-orange.svg)](https://files.minecraftforge.net/)
[![Fabric Supported](https://img.shields.io/badge/Loader-Fabric-blue.svg)](https://fabricmc.net/)
[![License MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **The ultimate multi-loader Minecraft randomizer mod. Randomize 16 different gameplay systems across Vanilla and Modded Minecraft with full automatic integration, custom seed support, weight controls, presets, and a clean in-game GUI!**

---

## 🌟 What is Universal Randomizer?

**Universal Randomizer** transforms your Minecraft world into an unpredictable adventure. Unlike traditional randomizer scripts or simple datapacks, Universal Randomizer is built from the ground up to **dynamically inspect and randomize every item, block, recipe, entity, loot table, and potion brew** in your game — including all content added by **other installed mods**!

Whether you want a casual randomized playthrough, a high-stakes speedrun challenge, or pure chaotic mayhem, Universal Randomizer gives you total control over what gets randomized and how.

---

## ⚡ Key Features

- 🎮 **16 Unique Randomization Modes:** From mining drops and entity spawns to potion brewing, advancements, and craft recipes.
- 🔌 **100% Modded Content Support:** Automatically detects and randomizes items, blocks, mobs, and recipes from any Forge or Fabric mod in your modpack!
- 🎨 **In-Game Interactive Setup GUI:** Configure modes, seed settings, and profiles directly upon world creation or at any time using `/randomizer setup`.
- ⚖️ **Weighted Drops & Custom Mappings:** Customize exact drop percentages (e.g. 70% Dirt, 20% Diamond, 10% Netherite) or force guaranteed item swaps.
- 📦 **6 Pre-Configured Preset Profiles:** One-click activation for profiles like *Speedrun*, *Chaos*, *Lucky Block*, *Vanilla+*, *Hardcore*, and *Streamer Mode*.
- 🌐 **Multiplayer & Dedicated Server Ready:** Server-authoritative sync — clients join and instantly play without needing local setup or extra client configs.
- 🚀 **Ultra-Optimized Performance:** Mappings generate once on world load with intelligent seed caching, keeping your server running clean with **< 1% TPS impact**.
- 📑 **Full Datapack & API Support:** Easily extend or override mappings via JSON datapacks or integrate directly using our Developer Java API.

---

## 🕹️ The 16 Randomizer Modes

Customize your experience by toggling any combination of the 16 available modes:

| Mode Icon | Mode Name | Description |
| :---: | | |
| ⛏️ | **Random Mining Drops** | Breaking any block drops a completely randomized item or block. |
| ⚔️ | **Random Mob Drops** | Defeating mobs drops randomized loot tables and custom items. |
| 📦 | **Random Chest Loot** | Dungeon, village, structure, and temple containers yield surprise loot tables. |
| 🛠️ | **Random Crafting** | Crafting table recipes produce randomized items while preserving recipe inputs. |
| 🔥 | **Random Smelting** | Furnace, blast furnace, and smoker outputs yield randomized smelted results. |
| 🎣 | **Random Fishing Loot** | Fishing hooks pull up random items, treasure, and junk. |
| 🧑‍🌾 | **Random Villager Trades** | Villager professions offer shuffled item trades and custom reward tiers. |
| ✨ | **Random Furnace XP** | Experience points generated from smelting items are randomized. |
| 🧱 | **Random Block Placement** | Placing a block down swaps it into a random block type. |
| 🌾 | **Random Crop Drops** | Harvesting wheat, carrots, potatoes, and crops yields randomized harvest items. |
| 🧟 | **Random Entity Spawns** | Naturally spawning or mob-spawner entities spawn as randomized mob types. |
| 🏰 | **Random Structure Loot** | All generated world structure loot tables are completely shuffled. |
| 🧪 | **Random Potion Brewing** | Brewing stand ingredient combinations yield unexpected potion effects. |
| 🔮 | **Random Enchantments** | Enchanting table options apply randomized enchantments and level bonuses. |
| 🔀 | **Random Loot Tables** | Comprehensive global loot table shuffle covering all remaining registries. |
| 🏆 | **Random Advancement Rewards**| Completing advancements awards random items, XP, or bonuses. |

---

## 📑 Preset Profiles

Jump straight into the action with built-in preset profiles tailored for different playstyles:

| Profile | Included Modes | Playstyle Summary |
|---|---|---|
| 🏃 **Speedrun** | Mining Drops, Mob Drops, Chest Loot | Fast-paced challenge balanced for quick progression and beat-the-dragon runs. |
| 💥 **Chaos** | **ALL 16 Modes** | Total absolute chaos. Everything that can be randomized IS randomized! |
| 🍀 **Lucky Block** | Mining Drops, Chest Loot | Classic mystery-block feel where every mined block is a surprise box. |
| 🌿 **Vanilla+** | Mob Drops, Crafting Recipes | Subtle variation that keeps world exploration and crafting fresh. |
| 💀 **Hardcore** | Mining, Mob Drops, Entity Spawns, Crafting | Survival test featuring unpredictable mob encounters and randomized drops. |
| 🎥 **Streamer Mode**| Mining, Mob Drops, Villagers, Crafting | Balanced for stream audience entertainment and engaging viewer clips. |

---

## 🖥️ In-Game Setup & Commands

### Interactive GUI
Upon starting a new singleplayer world or host session, an **intuitive Setup Screen** automatically prompts you to choose your preset profile, customize active modes, and select your randomization mode (Seed-Based, Pure Random, Shared, or Per-Player).

### Admin Commands (OP Level 2)

#### Mode Controls
```text
/randomizer setup                      - Open the interactive configuration GUI
/randomizer status                     - Show currently active modes and settings
/randomizer reset                      - Regenerate all randomizer seed mappings
/randomizer <mode_name> enable|disable - Quick toggle any of the 16 modes
```

#### Custom & Weighted Mappings
```text
/randomizer map block <source> <target>   - Direct swap a block (e.g. Stone -> Diamond Block)
/randomizer map item <source> <target>    - Direct swap an item output
/randomizer unmap <source>                - Remove custom forced mapping
/randomizer listmap                       - View all active fixed mappings

/randomizer weight <source> <target> <wt> - Set drop chance weight (e.g. Stone -> Dirt 70)
/randomizer weightlist <source>           - List active weighted drop table for an item
/randomizer weightclear <source>          - Clear custom weighted drops
```

#### Profile Management
```text
/randomizer profile save <Name>   - Save current configuration as a custom profile
/randomizer profile load <Name>   - Load a saved profile or default preset
/randomizer profile list         - Display all available saved profiles
/randomizer profile delete <Name>- Delete a saved profile
/randomizer profile export <Name>- Export profile data for sharing
```

---

## 📋 Datapack & Developer API

### Datapack Support
You can create custom datapack override files placed in `data/<namespace>/randomizer/mappings/<name>.json`:

```json
{
  "type": "block",
  "source": "minecraft:stone",
  "target": "minecraft:diamond",
  "weight": 0
}
```

### Developer Java API
Integrate or exclude your mod's content directly using the `RandomizerAPI`:

```java
import com.universalrandomizer.api.RandomizerAPI;
import com.universalrandomizer.config.RandomizerMode;

// Check if a specific mode is active
boolean isMiningRandomized = RandomizerAPI.isModeEnabled(RandomizerMode.MINING_DROPS);

// Exclude sensitive mod items from being randomized
RandomizerAPI.addToBlocklist(new ResourceLocation("yourmod", "quest_item"));
```

---

## 📥 Installation Requirements

### Required Dependencies
Ensure you have the following required mods installed for your loader:

| Dependency | Forge 1.20.1 | Fabric 1.20.1 |
|---|:---:|:---:|
| **[Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api)** | ✅ Required | ✅ Required |
| **[Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config)** | ✅ Required | ✅ Required |
| **[Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)** | — | ✅ Required |

1. Download **Universal Randomizer** for your respective modloader (Forge or Fabric).
2. Download the required dependencies listed above.
3. Drop all `.jar` files into your `.minecraft/mods` directory.
4. Launch the game and enjoy!

---

## 👥 Modpack & Server Usage

- **Can I include this mod in my modpack?**  
  **Yes!** You are 100% free to include Universal Randomizer in any public or private modpack on CurseForge, Modrinth, or custom launchers.
- **Is it required on the client for dedicated servers?**  
  Universal Randomizer operates server-side for logic and mapping sync. However, having it on the client allows full access to the setup GUIs. Server hosts can configure mappings globally without forcing client-side tweaks!

---

## 💬 Bug Reports & Support

If you encounter any issues, bugs, or have feature suggestions, feel free to submit an issue on our tracker or reach out on GitHub. Happy randomizing!

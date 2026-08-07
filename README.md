# Universal Randomizer

> **Randomize every Minecraft gameplay system — vanilla and modded, automatically.**

Universal Randomizer is a multi-loader Minecraft mod for **Forge 1.20.1** and **Fabric 1.20.1** that lets you randomize 16 different gameplay systems with full automatic support for any installed mod.

---

## ✨ Features

| Mode | Description |
|---|---|
| Random Mining Drops | Blocks drop random items |
| Random Mob Drops | Mobs drop random items on death |
| Random Chest Loot | Dungeon/structure containers get random loot tables |
| Random Crafting | Recipe outputs are shuffled |
| Random Smelting | Furnace results are randomized |
| Random Fishing Loot | Fish, treasure, and junk are randomized |
| Random Villager Trades | Trade outputs and professions are shuffled |
| Random Furnace XP | XP per smelt is randomized |
| Random Block Placement | Placed blocks are swapped |
| Random Crop Drops | Harvest items are randomized |
| Random Entity Spawns | Mobs spawn as random entity types |
| Random Structure Loot | All structure loot tables are shuffled |
| Random Potion Brewing | Brewing results are randomized |
| Random Enchantments | Enchanting table gives random enchantments |
| Random Loot Tables | Global catch-all loot table shuffle |
| Random Advancement Rewards | Advancement rewards are randomized |

---

## 🚀 Installation

### Required Dependencies
| Dependency | Forge | Fabric |
|---|---|---|
| [Architectury API](https://www.curseforge.com/minecraft/mc-mods/architectury-api) | ✅ | ✅ |
| [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config) | ✅ | ✅ |
| [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) | ❌ | ✅ |

Place the built jar and all dependencies in your `mods/` folder.

### Building from Source
```bash
# Requires Java 17 and internet access for dependency download
./gradlew :forge:build   # Outputs to forge/build/libs/
./gradlew :fabric:build  # Outputs to fabric/build/libs/
```

---

## 🎮 First Launch

When you create a new world, a **setup screen** will appear letting you:
- Select which modes to enable
- Search and filter modes by category
- Choose randomization type (Seed-based / Pure Random / Per Player / Shared)
- Apply a preset profile

---

## ⌨️ Commands

All commands require **OP level 2**.

### Mode Control
```
/randomizer mining enable|disable
/randomizer mobdrops enable|disable
/randomizer recipe enable|disable
/randomizer chestloot enable|disable
/randomizer smelting enable|disable
/randomizer fishing enable|disable
/randomizer villager enable|disable
/randomizer furnacexp enable|disable
/randomizer blockplacement enable|disable
/randomizer cropdrops enable|disable
/randomizer entityspawns enable|disable
/randomizer structureloot enable|disable
/randomizer potions enable|disable
/randomizer enchantments enable|disable
/randomizer loottables enable|disable
/randomizer advancements enable|disable
```

### Global
```
/randomizer reset           — Clear and regenerate all mappings
/randomizer setup           — Re-open the setup screen
/randomizer debug on|off    — Toggle debug logging
/randomizer export          — Export mapping to logs/randomizer_export.json
/randomizer status          — Show enabled modes
```

### Fixed Mappings
```
/randomizer map block minecraft:stone minecraft:diamond
/randomizer map item minecraft:iron_ore minecraft:netherite_ingot
/randomizer unmap minecraft:stone
/randomizer listmap
```

### Weighted Mappings
```
/randomizer weight minecraft:stone minecraft:dirt 70
/randomizer weight minecraft:stone minecraft:diamond 20
/randomizer weight minecraft:stone minecraft:netherite_ingot 10
/randomizer weightclear minecraft:stone
/randomizer weightlist minecraft:stone
```

### Profiles
```
/randomizer profile save Speedrun
/randomizer profile load Chaos
/randomizer profile list
/randomizer profile delete MyProfile
/randomizer profile export Speedrun
```

### Built-in Profiles
| Profile | Modes Enabled |
|---|---|
| Speedrun | Mining, Mob, Chest |
| Chaos | All 16 modes |
| Lucky Block | Mining, Chest |
| Vanilla+ | Mob, Crafting |
| Hardcore | Mining, Mob, Spawns, Crafting |
| Streamer Mode | Mining, Mob, Villager, Crafting |

---

## 🗂️ Datapack Support

Create files at `data/<namespace>/randomizer/mappings/<name>.json`:

```json
{
  "type": "block",
  "source": "minecraft:stone",
  "target": "minecraft:diamond",
  "weight": 0
}
```

For weighted mappings, set `weight > 0` and create multiple files with the same source.

---

## 🧩 API (for mod developers)

```java
// Add to your build.gradle
dependencies {
    compileOnly "com.universalrandomizer:universal-randomizer-common:1.0.0"
}
```

```java
import com.universalrandomizer.api.RandomizerAPI;
import com.universalrandomizer.config.RandomizerMode;

// Check if a mode is enabled
boolean miningEnabled = RandomizerAPI.isModeEnabled(RandomizerMode.MINING_DROPS);

// Get randomized drop for a block
ResourceLocation dropKey = RandomizerAPI.getMiningDropKey(Blocks.STONE);

// Exclude your mod's items from randomization
RandomizerAPI.addToBlocklist(new ResourceLocation("mymod", "special_item"));
```

---

## 📁 World Save Files

```
world/
└── randomizer/
    ├── settings.json    ← Mode config, seeds, custom mappings
    ├── mapping.json     ← Full generated mapping snapshot
    └── profiles/
        └── MyProfile.json
```

---

## ⚡ Performance

- All mappings generated **once** on world load — no per-tick overhead
- Target: **< 1% TPS loss**
- Lazy loading of per-player tables
- Memory-efficient ConcurrentHashMap caches

---

## 🔗 Multiplayer

- Host configures modes; clients sync automatically on join
- No client-side config required for players
- All commands require OP level 2

---

## 📜 License

MIT — see [LICENSE](LICENSE) for details.

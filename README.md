# 🎲 Universal Randomizer

[![Forge](https://img.shields.io/badge/Minecraft-Forge_1.20.1-orange.svg)](https://www.curseforge.com/minecraft/mc-mods)
[![Fabric](https://img.shields.io/badge/Minecraft-Fabric_1.20.1-blue.svg)](https://www.curseforge.com/minecraft/mc-mods)
[![Java 17](https://img.shields.io/badge/Java-17-brightgreen.svg)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **Randomize every Minecraft gameplay system — vanilla and modded, automatically.**

**Universal Randomizer** is the ultimate multi-loader Minecraft mod for **Forge 1.20.1** and **Fabric 1.20.1** that lets you randomize every major gameplay system with full automatic support for any installed mod.

---

## 🔌 Required Dependencies

| Dependency | Forge | Fabric | Link |
|---|:---:|:---:|---|
| **Architectury API** | ✅ | ✅ | [Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/architectury-api) |
| **Cloth Config API** | ✅ | ✅ | [Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/cloth-config) |
| **Fabric API** | ❌ | ✅ | [Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api) |

---

## ✨ Features & Randomizer Modes

| Icon | Mode | ID | Description |
|:---:|---|---|---|
| 🌾 | **Random Crop Drops** | `crop_drops` | Harvesting crops (Wheat, Carrots, Potatoes, Beetroots, Nether Wart, Cocoa, Sweet Berries, Melons, Pumpkins, Sugar Cane, Cactus, Torchflowers, Pitcher Plants) yields randomized loot items. |
| 💀 | **Random Death Drops** | `death_drops` | Items dropped from a player's inventory upon death transform into random items from the item pool. |
| 📦 | **Random Chest Loot** | `chest_loot` | Dungeon, village, fortress, end city, minecart, and structure chests fill with randomized loot. |
| ⛏️ | **Random Mining Drops** | `mining_drops` | Breaking blocks in the world drops randomized items. |
| ⚔️ | **Random Mob Drops** | `mob_drops` | Slaying mobs drops randomized items from the item pool. |
| 🔨 | **Random Crafting** | `crafting` | Crafting recipes yield randomized output items. |
| 🔥 | **Random Smelting** | `smelting` | Furnaces, blast furnaces, and smokers output randomized items during continuous bulk smelting. |
| 🎣 | **Random Fishing Loot** | `fishing_loot` | Reeling in your fishing line yields unexpected fish, treasure, or junk items. |
| 🧱 | **Random Block Placement** | `block_placement` | Placing a block transforms it into a random block type in the world. |
| 🐷 | **Random Entity Spawns** | `entity_spawns` | Natural and mob spawner spawns replace intended mobs with random mob types. |
| 🧪 | **Random Potion Brewing** | `potion_brewing` | Brewing stands produce randomized potion effects and items. |

---

## 🖥️ Modern In-Game Dashboard

Press **`R`** in-game or type `/randomizer` to open the interactive **Universal Randomizer Dashboard**:

- 🔍 **JEI / REI Style Item Picker**: Built-in 3D item grid with live search bar, page counters, and hover tooltips for selecting any item in Minecraft.
- ⚡ **Instant Presets**: Toggle pre-configured modes with a single click.
- ⚖️ **Weights & Profiles**: Fine-tune drop probabilities and export custom configurations.

---

## ⚡ Built-in Presets

| Profile | Description | Modes Enabled |
|---|---|---|
| **Chaos Default** | Pure randomized mayhem across all major modes | Mining, Mob, Chest, Crafting, Smelting, Crop Drops, Spawns, Death Drops |
| **Classic Randomizer** | Traditional loot-focused randomizer experience | Mining Drops, Mob Drops, Chest Loot |
| **Peaceful Farmer** | Relaxed gameplay focused on farming and processing | Crop Drops, Fishing Loot, Smelting Outputs |
| **World Craze** | World and survival mutation chaos | Block Placement, Entity Spawns, Death Drops |

---

## ⌨️ Controls & Commands

### Keybind
- **`R`** (Default): Opens the Universal Randomizer Hub Dashboard. (Rebindable in `Options -> Controls -> Key Binds -> Universal Randomizer`).

### Commands
- `/randomizer` or `/randomizer gui` — Opens the interactive dashboard.
- `/randomizer toggle <mode>` — Toggles a specific mode on/off in real time.
- `/randomizer preset <name>` — Loads a pre-configured preset profile.
- `/randomizer seed set <seed>` — Sets a custom seed for deterministic world randomization.
- `/randomizer reload` — Reloads configuration files.

---

## ⚙️ Compatibility & Performance

- **Platforms**: Supports **Forge** (47.4.16+) and **Fabric** (0.92.6+).
- **Modpack Compatible**: Tested with **JEI**, **REI**, **EMI**, **GeckoLib**, **Sodium**, **Embeddium**, and **Sinytra Connector**.
- **Crash Prevention**: Features built-in thread safety (`ThreadLocal` flags) and projectile filters to prevent conflicts with weapon and gun mods.

---

## 📜 Installation

1. Download the version corresponding to your mod loader (**Forge** or **Fabric**).
2. Install the required dependency mods (**Architectury API**, **Cloth Config**, and **Fabric API** if on Fabric).
3. Drop the `.jar` files into your `mods/` directory.
4. Launch Minecraft, press **`R`**, and enjoy!

package com.universalrandomizer.config;

/**
 * Enumerates every randomization mode supported by Universal Randomizer.
 * Each mode corresponds to a distinct gameplay system that can be independently toggled.
 */
public enum RandomizerMode {

    // ── Drop / Loot modes ──────────────────────────────────────────────────────
    MINING_DROPS(
        "mining_drops",
        "Random Mining Drops",
        "Randomizes items dropped when breaking blocks.",
        Category.DROPS
    ),
    MOB_DROPS(
        "mob_drops",
        "Random Mob Drops",
        "Randomizes loot dropped by all mobs on death.",
        Category.DROPS
    ),
    CROP_DROPS(
        "crop_drops",
        "Random Crop Drops",
        "Randomizes the harvest output of all crop blocks.",
        Category.DROPS
    ),
    FISHING_LOOT(
        "fishing_loot",
        "Random Fishing Loot",
        "Randomizes fish, treasure, and junk caught while fishing.",
        Category.DROPS
    ),
    CHEST_LOOT(
        "chest_loot",
        "Random Chest Loot",
        "Randomizes loot found inside generated structures and chests across the world.",
        Category.DROPS
    ),
    DEATH_DROPS(
        "death_drops",
        "Random Death Drops",
        "Randomizes items dropped from player inventory on death.",
        Category.DROPS
    ),

    // ── Crafting modes ─────────────────────────────────────────────────────────
    CRAFTING(
        "crafting",
        "Random Crafting",
        "Randomizes the output of all crafting recipes.",
        Category.CRAFTING
    ),
    SMELTING(
        "smelting",
        "Random Smelting",
        "Randomizes results from furnaces, blast furnaces, and smokers.",
        Category.CRAFTING
    ),

    // ── World modes ────────────────────────────────────────────────────────────
    BLOCK_PLACEMENT(
        "block_placement",
        "Random Block Placement",
        "Randomizes what block is actually placed when the player places a block.",
        Category.WORLD
    ),
    ENTITY_SPAWNS(
        "entity_spawns",
        "Random Entity Spawns",
        "Randomizes which entity type spawns in place of intended spawns.",
        Category.WORLD
    ),
    STRUCTURE_SPAWNS(
        "structure_spawns",
        "Random Structure Spawn",
        "Randomizes structure generation and structure types across the world.",
        Category.WORLD
    ),

    // ── Magic / Other modes ────────────────────────────────────────────────────
    POTION_BREWING(
        "potion_brewing",
        "Random Potion Brewing",
        "Randomizes the potions produced by the brewing stand.",
        Category.OTHER
    );

    // ──────────────────────────────────────────────────────────────────────────

    /** Groups for UI display. */
    public enum Category {
        DROPS("Drops"),
        CRAFTING("Crafting"),
        WORLD("World"),
        OTHER("Other");

        private final String displayName;
        Category(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // ──────────────────────────────────────────────────────────────────────────

    private final String id;
    private final String displayName;
    private final String description;
    private final Category category;

    RandomizerMode(String id, String displayName, String description, Category category) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Category getCategory()  { return category; }

    /** Look up a mode by its serialized string ID. Returns null if not found. */
    public static RandomizerMode fromId(String id) {
        for (RandomizerMode mode : values()) {
            if (mode.id.equals(id)) return mode;
        }
        return null;
    }
}

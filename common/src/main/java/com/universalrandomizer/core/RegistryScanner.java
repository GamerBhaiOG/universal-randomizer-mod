package com.universalrandomizer.core;

import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootDataManager;

import java.util.*;

/**
 * Scans every Minecraft registry to build domain-specific pools of
 * {@link ResourceLocation} keys.  These pools are the universe from which
 * {@link MappingGenerator} creates randomized mappings.
 *
 * <p>All filtering (blocklists, spawn eggs, etc.) happens here so that
 * downstream code never needs to worry about illegal mappings.
 */
public class RegistryScanner {

    // ── Hard-coded blocklist entries ───────────────────────────────────────────

    /** Blocks that must never be randomized (structural/technical blocks). */
    private static final Set<Block> BLOCK_BLOCKLIST = Set.of(
        Blocks.AIR,
        Blocks.CAVE_AIR,
        Blocks.VOID_AIR,
        Blocks.BARRIER,
        Blocks.STRUCTURE_VOID,
        Blocks.STRUCTURE_BLOCK,
        Blocks.BEDROCK,
        Blocks.COMMAND_BLOCK,
        Blocks.CHAIN_COMMAND_BLOCK,
        Blocks.REPEATING_COMMAND_BLOCK,
        Blocks.JIGSAW,
        Blocks.MOVING_PISTON,
        Blocks.PISTON_HEAD,
        Blocks.LIGHT
    );

    /** Items that must never be randomized. */
    private static final Set<Item> ITEM_BLOCKLIST = Set.of(
        Items.AIR,
        Items.BARRIER,
        Items.STRUCTURE_VOID,
        Items.STRUCTURE_BLOCK,
        Items.COMMAND_BLOCK,
        Items.CHAIN_COMMAND_BLOCK,
        Items.REPEATING_COMMAND_BLOCK,
        Items.JIGSAW,
        Items.DEBUG_STICK,
        Items.KNOWLEDGE_BOOK,
        Items.BUNDLE // experimental in 1.20.1
    );

    /** Modder-extensible runtime blocklist for items/blocks by ResourceLocation. */
    private static final Set<ResourceLocation> RUNTIME_BLOCKLIST = new LinkedHashSet<>();

    // ──────────────────────────────────────────────────────────────────────────

    // Result pools — populated by scan()
    private final List<ResourceLocation> itemPool        = new ArrayList<>();
    private final List<ResourceLocation> blockPool       = new ArrayList<>();
    private final List<ResourceLocation> entityPool      = new ArrayList<>();
    private final List<ResourceLocation> spawnEggPool    = new ArrayList<>();
    private final List<ResourceLocation> enchantmentPool = new ArrayList<>();
    private final List<ResourceLocation> potionPool      = new ArrayList<>();

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Runs the full registry scan.  Call this once when a world is first loaded.
     *
     * @param lootDataManager the server-side loot data manager (used by caller to enumerate loot table keys)
     */
    public void scan(LootDataManager lootDataManager) {
        long start = System.currentTimeMillis();
        RandomizerLogger.debug("RegistryScanner: starting registry scan...");

        scanItems();
        scanBlocks();
        scanEntities();
        scanEnchantments();
        scanPotions();

        long elapsed = System.currentTimeMillis() - start;
        RandomizerLogger.debug("RegistryScanner: scan complete in {}ms — items={}, blocks={}, entities={}, enchants={}, potions={}",
            elapsed, itemPool.size(), blockPool.size(), entityPool.size(), enchantmentPool.size(), potionPool.size());
    }

    // ── Private scan helpers ───────────────────────────────────────────────────

    private void scanItems() {
        itemPool.clear();
        spawnEggPool.clear();
        for (Map.Entry<net.minecraft.core.Holder.Reference<Item>, Item> entry :
                BuiltInRegistries.ITEM.holders()
                    .filter(h -> h.value() != null)
                    .map(h -> Map.entry(h, h.value()))
                    .toList()
                    .stream()
                    .filter(e -> !ITEM_BLOCKLIST.contains(e.getValue()))
                    .filter(e -> !isRuntimeBlocked(BuiltInRegistries.ITEM.getKey(e.getValue())))
                    .toList()) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.getValue());
            if (entry.getValue() instanceof SpawnEggItem) {
                spawnEggPool.add(key);
            } else {
                itemPool.add(key);
            }
        }
        Collections.sort(itemPool);
        Collections.sort(spawnEggPool);
    }

    private void scanBlocks() {
        blockPool.clear();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (BLOCK_BLOCKLIST.contains(block)) continue;
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            if (key == null || isRuntimeBlocked(key)) continue;
            blockPool.add(key);
        }
        Collections.sort(blockPool);
    }

    private void scanEntities() {
        entityPool.clear();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (key == null || isRuntimeBlocked(key)) continue;
            // Exclude technical entity types
            if (key.getPath().equals("player") || key.getPath().equals("fishing_bobber")
                    || key.getPath().equals("item") || key.getPath().equals("experience_orb")) continue;
            entityPool.add(key);
        }
        Collections.sort(entityPool);
    }

    private void scanEnchantments() {
        enchantmentPool.clear();
        for (var entry : BuiltInRegistries.ENCHANTMENT.entrySet()) {
            ResourceLocation key = entry.getKey().location();
            if (!isRuntimeBlocked(key)) enchantmentPool.add(key);
        }
        Collections.sort(enchantmentPool);
    }

    private void scanPotions() {
        potionPool.clear();
        for (var entry : BuiltInRegistries.POTION.entrySet()) {
            ResourceLocation key = entry.getKey().location();
            // Skip empty/water potions as targets (they can still be sources)
            String path = key.getPath();
            if (path.equals("empty") || path.equals("water")) continue;
            if (!isRuntimeBlocked(key)) potionPool.add(key);
        }
        Collections.sort(potionPool);
    }

    // ──────────────────────────────────────────────────────────────────────────

    private static boolean isRuntimeBlocked(ResourceLocation key) {
        return key != null && RUNTIME_BLOCKLIST.contains(key);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Allows other mods (via {@link com.universalrandomizer.api.RandomizerAPI}) to exclude entries. */
    public static void addRuntimeBlocklist(ResourceLocation key) {
        RUNTIME_BLOCKLIST.add(key);
    }

    public List<ResourceLocation> getItemPool()        { return Collections.unmodifiableList(itemPool); }
    public List<ResourceLocation> getBlockPool()       { return Collections.unmodifiableList(blockPool); }
    public List<ResourceLocation> getEntityPool()      { return Collections.unmodifiableList(entityPool); }
    public List<ResourceLocation> getSpawnEggPool()    { return Collections.unmodifiableList(spawnEggPool); }
    public List<ResourceLocation> getEnchantmentPool() { return Collections.unmodifiableList(enchantmentPool); }
    public List<ResourceLocation> getPotionPool()      { return Collections.unmodifiableList(potionPool); }
}

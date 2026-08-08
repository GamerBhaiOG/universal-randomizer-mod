package com.universalrandomizer.core;

import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe runtime cache for all generated randomization mappings.
 *
 * <p>Each domain (items, blocks, entities, etc.) has its own table.
 * Custom (command-set) mappings take priority over generated ones.
 *
 * <p>Per-player mappings are stored in a nested map keyed by player UUID.
 */
public class MappingTable {

    // ── Domain tables (ResourceLocation → ResourceLocation) ───────────────────

    /** Block mining drops: source block RL → target item RL */
    private final Map<ResourceLocation, ResourceLocation> miningDrops   = new ConcurrentHashMap<>();
    /** Mob entity drops: source entity RL → target item RL */
    private final Map<ResourceLocation, ResourceLocation> mobDrops      = new ConcurrentHashMap<>();
    /** Crafting recipe outputs: recipe RL → target item RL */
    private final Map<ResourceLocation, ResourceLocation> craftingOutputs = new ConcurrentHashMap<>();
    /** Smelting inputs: ingredient item RL → target item RL */
    private final Map<ResourceLocation, ResourceLocation> smeltingOutputs = new ConcurrentHashMap<>();
    /** Fishing loot table RL → target loot table RL */
    private final Map<ResourceLocation, ResourceLocation> fishingLoot   = new ConcurrentHashMap<>();
    /** Entity spawn RL → replacement entity RL */
    private final Map<ResourceLocation, ResourceLocation> entitySpawns  = new ConcurrentHashMap<>();
    /** Potion RL → target potion RL */
    private final Map<ResourceLocation, ResourceLocation> potions       = new ConcurrentHashMap<>();
    /** Chest/Structure loot table RL → replacement item or loot table RL */
    private final Map<ResourceLocation, ResourceLocation> chestLoot      = new ConcurrentHashMap<>();
    /** Block placement: placed block RL → actual block placed RL */
    private final Map<ResourceLocation, ResourceLocation> blockPlacements = new ConcurrentHashMap<>();
    /** Crop drops: crop block or crop drop item RL → target item RL */
    private final Map<ResourceLocation, ResourceLocation> cropDrops     = new ConcurrentHashMap<>();
    /** Structure spawn RL → replacement structure RL */
    private final Map<ResourceLocation, ResourceLocation> structureSpawns = new ConcurrentHashMap<>();

    // ── Weighted overrides: source RL → list of weighted targets ──────────────
    private final Map<ResourceLocation, List<WeightedEntry<ResourceLocation>>> weightedMappings =
        new ConcurrentHashMap<>();

    // ── Custom (command-set) overrides (highest priority) ─────────────────────
    private final Map<ResourceLocation, ResourceLocation> customMappings = new ConcurrentHashMap<>();

    // ── Per-player mappings (UUID string → domain → mapping) ──────────────────
    private final Map<String, Map<String, Map<ResourceLocation, ResourceLocation>>> perPlayerMappings =
        new ConcurrentHashMap<>();

    // ──────────────────────────────────────────────────────────────────────────

    // ── Domain accessors ──────────────────────────────────────────────────────

    public Map<ResourceLocation, ResourceLocation> getMiningDrops()       { return miningDrops; }
    public Map<ResourceLocation, ResourceLocation> getMobDrops()          { return mobDrops; }
    public Map<ResourceLocation, ResourceLocation> getCraftingOutputs()   { return craftingOutputs; }
    public Map<ResourceLocation, ResourceLocation> getSmeltingOutputs()   { return smeltingOutputs; }
    public Map<ResourceLocation, ResourceLocation> getFishingLoot()       { return fishingLoot; }
    public Map<ResourceLocation, ResourceLocation> getEntitySpawns()      { return entitySpawns; }
    public Map<ResourceLocation, ResourceLocation> getPotions()           { return potions; }
    public Map<ResourceLocation, ResourceLocation> getChestLoot()         { return chestLoot; }
    public Map<ResourceLocation, ResourceLocation> getBlockPlacements()   { return blockPlacements; }
    public Map<ResourceLocation, ResourceLocation> getCropDrops()         { return cropDrops; }
    public Map<ResourceLocation, ResourceLocation> getStructureSpawns()   { return structureSpawns; }

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Looks up a mapping for the given key in priority order:
     * 1. Weighted overrides (if present, samples probabilistically)
     * 2. Custom command-set overrides
     * 3. Generated table (domain-specific)
     * Falls back to {@code key} itself if not mapped.
     */
    public ResourceLocation lookup(Map<ResourceLocation, ResourceLocation> domainTable, ResourceLocation key, Random rng) {
        // 1. Weighted
        List<WeightedEntry<ResourceLocation>> weighted = weightedMappings.get(key);
        if (weighted != null && !weighted.isEmpty()) {
            int totalWeight = WeightedEntry.totalWeight(weighted);
            int roll = rng.nextInt(totalWeight);
            return WeightedEntry.select(weighted, roll);
        }
        // 2. Custom
        if (customMappings.containsKey(key)) return customMappings.get(key);
        // 3. Generated
        return domainTable.getOrDefault(key, key);
    }

    // ──────────────────────────────────────────────────────────────────────────

    /** Sets a custom mapping (highest priority), overriding generation. */
    public void putCustom(ResourceLocation source, ResourceLocation target) {
        customMappings.put(source, target);
    }

    /** Removes a custom mapping (falls back to generated). */
    public void removeCustom(ResourceLocation source) {
        customMappings.remove(source);
    }

    /** Returns all current custom mappings (read-only view). */
    public Map<ResourceLocation, ResourceLocation> getCustomMappings() {
        return Collections.unmodifiableMap(customMappings);
    }

    // ──────────────────────────────────────────────────────────────────────────

    /** Adds or replaces a weighted mapping for a source key. */
    public void putWeighted(ResourceLocation source, List<WeightedEntry<ResourceLocation>> entries) {
        weightedMappings.put(source, new ArrayList<>(entries));
    }

    /** Removes the weighted mapping for a source key. */
    public void removeWeighted(ResourceLocation source) {
        weightedMappings.remove(source);
    }

    /** Returns all weighted mappings (read-only). */
    public Map<ResourceLocation, List<WeightedEntry<ResourceLocation>>> getWeightedMappings() {
        return Collections.unmodifiableMap(weightedMappings);
    }

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Bulk-loads a generated mapping into a domain table.
     * Existing entries are replaced.
     */
    public void loadGenerated(Map<ResourceLocation, ResourceLocation> domainTable,
                               Map<ResourceLocation, ResourceLocation> generated) {
        domainTable.putAll(generated);
    }

    /** Clears all generated mappings (does NOT clear custom or weighted). */
    public void clearGenerated() {
        miningDrops.clear();
        mobDrops.clear();
        craftingOutputs.clear();
        smeltingOutputs.clear();
        fishingLoot.clear();
        entitySpawns.clear();
        potions.clear();
        chestLoot.clear();
        blockPlacements.clear();
        cropDrops.clear();
        structureSpawns.clear();
    }

    /** Fully resets all mappings including custom and weighted. */
    public void clearAll() {
        clearGenerated();
        customMappings.clear();
        weightedMappings.clear();
        perPlayerMappings.clear();
    }

    // ──────────────────────────────────────────────────────────────────────────

    /** Returns the per-player domain map for the given player UUID string. */
    public Map<ResourceLocation, ResourceLocation> getPerPlayerDomain(String uuid, String domain) {
        return perPlayerMappings
            .computeIfAbsent(uuid, u -> new ConcurrentHashMap<>())
            .computeIfAbsent(domain, d -> new ConcurrentHashMap<>());
    }

    /** Clears all per-player mappings (e.g. on server restart). */
    public void clearPerPlayer() {
        perPlayerMappings.clear();
    }

    // ──────────────────────────────────────────────────────────────────────────

    /** Returns total number of generated mappings across all domains. */
    public int totalGeneratedMappings() {
        return miningDrops.size() + mobDrops.size() + craftingOutputs.size()
             + smeltingOutputs.size() + fishingLoot.size()
             + entitySpawns.size() + potions.size()
             + chestLoot.size() + blockPlacements.size() + cropDrops.size()
             + structureSpawns.size();
    }
}

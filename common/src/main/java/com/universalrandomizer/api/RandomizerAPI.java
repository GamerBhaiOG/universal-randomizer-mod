package com.universalrandomizer.api;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.core.RegistryScanner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Public API for other mods to interact with Universal Randomizer.
 *
 * <p>All methods are null-safe and return the original value when the randomizer
 * is not initialized or the relevant mode is disabled.
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * // Get the randomized drop for a block
 * ResourceLocation drop = RandomizerAPI.getMiningDropKey(Blocks.STONE);
 *
 * // Check if mining drops are active
 * if (RandomizerAPI.isModeEnabled(RandomizerMode.MINING_DROPS)) { ... }
 *
 * // Exclude a custom item from the randomization pool
 * RandomizerAPI.addToBlocklist(new ResourceLocation("mymod", "special_item"));
 * }</pre>
 */
public final class RandomizerAPI {

    private RandomizerAPI() {}

    // ── Mode queries ───────────────────────────────────────────────────────────

    /** Returns true if the given randomizer mode is enabled on the current server. */
    public static boolean isModeEnabled(RandomizerMode mode) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        return mgr.isInitialized() && mgr.isEnabled(mode);
    }

    // ── Mining drops ──────────────────────────────────────────────────────────

    /**
     * Returns the ResourceLocation of the item that will drop from the given block.
     * Returns the block's own key if mining drops are disabled or unmapped.
     */
    public static ResourceLocation getMiningDropKey(Block block) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.MINING_DROPS)) {
            return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        }
        return mgr.getMiningDrop(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block));
    }

    // ── Mob drops ─────────────────────────────────────────────────────────────

    /**
     * Returns the ResourceLocation of the item that will drop from the given entity type.
     */
    public static ResourceLocation getMobDropKey(EntityType<?> entityType) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.MOB_DROPS)) {
            return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        }
        return mgr.getMobDrop(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    // ── Recipe output ─────────────────────────────────────────────────────────

    /**
     * Returns the ResourceLocation of the item produced by the given recipe ID.
     */
    public static ResourceLocation getRecipeOutputKey(ResourceLocation recipeId) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.CRAFTING)) return recipeId;
        return mgr.getCraftingOutput(recipeId);
    }

    // ── Loot table ────────────────────────────────────────────────────────────

    /**
     * Returns the randomized loot table ResourceLocation that replaces the given one.
     */
    public static ResourceLocation getLootTable(ResourceLocation original) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.CHEST_LOOT)) return original;
        return mgr.getChestLoot(original);
    }

    // ── Blocklist ─────────────────────────────────────────────────────────────

    /**
     * Adds a ResourceLocation to the runtime blocklist, preventing it from
     * appearing as a source or target in any randomized mapping.
     *
     * <p>Must be called before the world finishes loading (e.g. during mod init).
     */
    public static void addToBlocklist(ResourceLocation id) {
        RegistryScanner.addRuntimeBlocklist(id);
    }

    // ── Raw table access ──────────────────────────────────────────────────────

    /**
     * Returns the full mining drop mapping table (read-only view).
     * Returns an empty map if not initialized.
     */
    public static java.util.Map<ResourceLocation, ResourceLocation> getMiningDropTable() {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized()) return java.util.Collections.emptyMap();
        return java.util.Collections.unmodifiableMap(mgr.getTable().getMiningDrops());
    }
}

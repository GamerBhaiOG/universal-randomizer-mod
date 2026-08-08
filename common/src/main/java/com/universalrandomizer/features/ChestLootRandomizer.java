package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Randomizes loot table outputs for chest loot and structure chests across the world.
 */
public final class ChestLootRandomizer {

    private ChestLootRandomizer() {}

    /**
     * Applies loot table redirection for structure/chest loot tables.
     */
    public static ResourceLocation applyLootTable(ResourceLocation tableId) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.CHEST_LOOT)) return tableId;

        ResourceLocation mapped = mgr.getChestLoot(tableId);
        if (!mapped.equals(tableId)) {
            RandomizerLogger.debug("ChestLoot Table: {} -> {}", tableId, mapped);
        }
        return mapped;
    }

    /**
     * Randomizes an item stack found in structure chests.
     */
    public static ItemStack randomizeChestItem(ItemStack vanillaItem) {
        if (vanillaItem == null || vanillaItem.isEmpty()) return vanillaItem;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.CHEST_LOOT)) return vanillaItem;

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(vanillaItem.getItem());
        if (itemKey == null) return vanillaItem;

        ResourceLocation targetKey = mgr.getChestLoot(itemKey);
        if (targetKey == null || targetKey.equals(itemKey)) {
            targetKey = mgr.getTable().lookup(
                mgr.getTable().getMiningDrops(), itemKey, new java.util.Random());
        }

        final ResourceLocation finalKey = targetKey;
        return BuiltInRegistries.ITEM.getOptional(finalKey)
            .map(item -> {
                ItemStack result = new ItemStack(item, vanillaItem.getCount());
                RandomizerLogger.debug("ChestLoot Item: {} -> {}", itemKey, finalKey);
                return result;
            })
            .orElse(vanillaItem);
    }
}

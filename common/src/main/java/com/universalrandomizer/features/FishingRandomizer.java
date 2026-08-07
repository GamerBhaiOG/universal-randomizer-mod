package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Randomizes fishing loot by swapping caught items.
 */
public final class FishingRandomizer {

    private FishingRandomizer() {}

    /** Randomizes a caught fishing item stack. */
    public static ItemStack randomizeFishingItem(ItemStack vanillaItem) {
        if (vanillaItem == null || vanillaItem.isEmpty()) return vanillaItem;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.FISHING_LOOT)) return vanillaItem;

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(vanillaItem.getItem());
        if (itemKey == null) return vanillaItem;

        ResourceLocation targetKey = mgr.getFishingLoot(itemKey);
        if (targetKey == null || targetKey.equals(itemKey)) {
            targetKey = mgr.getTable().lookup(
                mgr.getTable().getMiningDrops(), itemKey, new java.util.Random());
        }

        ResourceLocation finalKey = targetKey;
        return BuiltInRegistries.ITEM.getOptional(finalKey)
            .map(item -> {
                ItemStack result = new ItemStack(item, vanillaItem.getCount());
                RandomizerLogger.debug("Fishing Caught: {} -> {}", itemKey, finalKey);
                return result;
            })
            .orElse(vanillaItem);
    }
}

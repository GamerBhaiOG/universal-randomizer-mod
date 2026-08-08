package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Randomizes items dropped from player inventory upon death.
 */
public final class DeathDropRandomizer {

    private DeathDropRandomizer() {}

    /**
     * Randomizes a player death drop item stack.
     */
    public static ItemStack randomizeDeathDrop(ItemStack vanillaItem) {
        if (vanillaItem == null || vanillaItem.isEmpty()) return vanillaItem;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.DEATH_DROPS)) return vanillaItem;

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(vanillaItem.getItem());
        if (itemKey == null) return vanillaItem;

        ResourceLocation targetKey = mgr.getTable().lookup(
            mgr.getTable().getMiningDrops(), itemKey, new java.util.Random());

        final ResourceLocation finalKey = targetKey;
        return BuiltInRegistries.ITEM.getOptional(finalKey)
            .map(item -> {
                ItemStack result = new ItemStack(item, vanillaItem.getCount());
                if (vanillaItem.hasTag()) {
                    result.setTag(vanillaItem.getTag().copy());
                }
                RandomizerLogger.debug("Death Drop: {} -> {} (x{})", itemKey, finalKey, result.getCount());
                return result;
            })
            .orElse(vanillaItem);
    }
}

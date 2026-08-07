package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Handles randomization of block mining drops.
 * Intercepts dropped item stacks and transforms their item type according to the mapping table.
 */
public final class MiningDropRandomizer {

    private MiningDropRandomizer() {}

    /** Applies the mining drop mapping to the given item stack. */
    public static ItemStack applyDrop(Block source, ItemStack originalStack) {
        if (originalStack == null || originalStack.isEmpty()) return originalStack;
        return applyDrop(originalStack.getItem(), originalStack);
    }

    /** Applies the mining drop mapping based on the dropped item type. */
    public static ItemStack applyDrop(Item item, ItemStack originalStack) {
        if (originalStack == null || originalStack.isEmpty() || item == Items.AIR) return originalStack;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.MINING_DROPS)) {
            return originalStack;
        }

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(item);
        if (itemKey == null) return originalStack;

        ResourceLocation targetItemKey = mgr.getMiningDrop(itemKey);
        if (targetItemKey.equals(itemKey)) return originalStack;

        return BuiltInRegistries.ITEM.getOptional(targetItemKey)
            .map(target -> {
                ItemStack result = new ItemStack(target, originalStack.getCount());
                RandomizerLogger.debug("MiningDrop: {} -> {} (x{})", itemKey, targetItemKey, result.getCount());
                return result;
            })
            .orElse(originalStack);
    }

    public static ResourceLocation getMappedKey(ResourceLocation itemKey) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.MINING_DROPS)) return itemKey;
        return mgr.getMiningDrop(itemKey);
    }
}

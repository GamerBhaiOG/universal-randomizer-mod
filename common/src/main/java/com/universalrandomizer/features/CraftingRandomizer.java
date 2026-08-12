package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


/**
 * Randomizes crafting recipe outputs across Crafting Table, Inventory 2x2,
 * Stonecutter, Smithing Table, and custom tables.
 */
public final class CraftingRandomizer {

    private CraftingRandomizer() {}

    /**
     * Randomizes an output {@link ItemStack} based on the item type's key.
     * Preserves the original stack count.
     */
    public static ItemStack randomizeStack(ItemStack vanillaOutput) {
        if (vanillaOutput == null || vanillaOutput.isEmpty()) return vanillaOutput;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.CRAFTING)) return vanillaOutput;

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(vanillaOutput.getItem());
        if (itemKey == null) return vanillaOutput;

        ResourceLocation targetKey = mgr.getCraftingOutput(itemKey);
        if (targetKey == null || targetKey.equals(itemKey)) return vanillaOutput;

        return BuiltInRegistries.ITEM.getOptional(targetKey)
            .map(item -> {
                ItemStack result = new ItemStack(item, vanillaOutput.getCount());
                if (vanillaOutput.hasTag()) {
                    result.setTag(vanillaOutput.getTag().copy());
                }
                RandomizerLogger.debug("Crafting: {} -> {} (x{})", itemKey, targetKey, result.getCount());
                return result;
            })
            .orElse(vanillaOutput);
    }

    public static ItemStack applyOutput(Object recipe, ItemStack vanillaOutput) {
        return randomizeStack(vanillaOutput);
    }
}

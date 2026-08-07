package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;

/**
 * Randomizes items dropped when crops are harvested across all crop block types.
 */
public final class CropDropRandomizer {

    private CropDropRandomizer() {}

    /**
     * Checks if a given block is a crop or farm plant block.
     */
    public static boolean isCropBlock(Block block) {
        return block instanceof CropBlock
            || block instanceof NetherWartBlock
            || block instanceof CocoaBlock
            || block instanceof SweetBerryBushBlock
            || block instanceof MelonBlock
            || block instanceof PumpkinBlock
            || block instanceof SugarCaneBlock
            || block instanceof CactusBlock
            || block.getClass().getSimpleName().toLowerCase().contains("crop");
    }

    /**
     * Applies item-level randomization for crop drops.
     */
    public static ItemStack applyDrop(Block cropBlock, ItemStack original) {
        if (original == null || original.isEmpty()) return original;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.CROP_DROPS)) return original;

        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(cropBlock);
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(original.getItem());

        ResourceLocation lookupKey = blockKey != null ? blockKey : itemKey;
        if (lookupKey == null) return original;

        ResourceLocation targetKey = mgr.getCropDrop(lookupKey);
        if (targetKey == null || targetKey.equals(lookupKey)) {
            // Fallback to random item from item pool
            targetKey = mgr.getTable().lookup(
                mgr.getTable().getMiningDrops(), lookupKey, new java.util.Random());
        }

        ResourceLocation finalKey = targetKey;
        return BuiltInRegistries.ITEM.getOptional(finalKey)
            .map(item -> {
                ItemStack result = new ItemStack(item, original.getCount());
                RandomizerLogger.debug("Crop Harvest: {} -> {} (x{})", lookupKey, finalKey, result.getCount());
                return result;
            })
            .orElse(original);
    }
}

package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;

import java.util.Set;

/**
 * Randomizes items dropped when crops are harvested across all crop block types.
 */
public final class CropDropRandomizer {

    private CropDropRandomizer() {}

    private static final Set<Item> CROP_ITEMS = Set.of(
        Items.WHEAT, Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO,
        Items.BEETROOT, Items.BEETROOT_SEEDS, Items.NETHER_WART,
        Items.COCOA_BEANS, Items.SWEET_BERRIES, Items.MELON_SLICE,
        Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.PUMPKIN,
        Items.SUGAR_CANE, Items.CACTUS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD
    );

    /**
     * Overloaded helper: checks if a given block is a crop or farm plant block.
     */
    public static boolean isCropBlock(Block block) {
        return isCropBlock(block, null);
    }

    /**
     * Checks if a given block or item is a crop or farm plant block.
     */
    public static boolean isCropBlock(Block block, ItemStack droppedItem) {
        if (block instanceof CropBlock
            || block instanceof NetherWartBlock
            || block instanceof CocoaBlock
            || block instanceof SweetBerryBushBlock
            || block instanceof MelonBlock
            || block instanceof PumpkinBlock
            || block instanceof SugarCaneBlock
            || block instanceof CactusBlock
            || block instanceof BushBlock
            || block.getClass().getSimpleName().toLowerCase().contains("crop")
            || block.getClass().getSimpleName().toLowerCase().contains("bush")) {
            return true;
        }

        if (droppedItem != null && !droppedItem.isEmpty()) {
            Item item = droppedItem.getItem();
            if (CROP_ITEMS.contains(item)) return true;
            String itemName = BuiltInRegistries.ITEM.getKey(item).getPath().toLowerCase();
            return itemName.contains("seed") || itemName.contains("crop") || itemName.contains("wart");
        }

        return false;
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
            targetKey = mgr.getTable().lookup(
                mgr.getTable().getMiningDrops(), lookupKey, new java.util.Random());
        }

        final ResourceLocation finalKey = targetKey;
        return BuiltInRegistries.ITEM.getOptional(finalKey)
            .map(item -> {
                ItemStack result = new ItemStack(item, original.getCount());
                RandomizerLogger.debug("Crop Harvest: {} -> {} (x{})", lookupKey, finalKey, result.getCount());
                return result;
            })
            .orElse(original);
    }
}

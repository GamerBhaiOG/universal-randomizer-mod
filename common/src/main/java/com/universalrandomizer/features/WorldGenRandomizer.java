package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Randomizes world generation features, ores, trees, and feature block placement.
 */
public final class WorldGenRandomizer {

    private WorldGenRandomizer() {}

    /**
     * Randomizes a block state generated during world generation feature placement.
     */
    public static BlockState randomizePlacedBlock(BlockState originalState) {
        if (originalState == null || originalState.isAir()) return originalState;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.WORLD_GEN)) return originalState;

        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(originalState.getBlock());
        if (blockKey == null) return originalState;

        ResourceLocation targetKey = mgr.getTable().lookup(
            mgr.getTable().getWorldGen(), blockKey, new java.util.Random());

        if (targetKey == null || targetKey.equals(blockKey)) return originalState;

        return BuiltInRegistries.BLOCK.getOptional(targetKey)
            .map(Block::defaultBlockState)
            .orElse(originalState);
    }
}

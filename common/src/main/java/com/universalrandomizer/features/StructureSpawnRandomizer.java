package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Randomizes structure generation and structure block themes across all dimensions.
 */
public final class StructureSpawnRandomizer {

    private StructureSpawnRandomizer() {}

    /**
     * Randomizes a structure type RL during world structure placement.
     */
    public static ResourceLocation getMappedStructure(ResourceLocation originalStructureKey) {
        if (originalStructureKey == null) return null;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.STRUCTURE_SPAWNS)) return originalStructureKey;

        ResourceLocation targetKey = mgr.getTable().lookup(
            mgr.getTable().getStructureSpawns(), originalStructureKey, new java.util.Random());

        if (targetKey != null && !targetKey.equals(originalStructureKey)) {
            RandomizerLogger.debug("Structure Spawn: {} -> {}", originalStructureKey, targetKey);
            return targetKey;
        }
        return originalStructureKey;
    }

    /**
     * Randomizes a structure block state during structure piece placement across dimensions.
     */
    public static BlockState randomizeStructureBlock(BlockState originalState) {
        if (originalState == null || originalState.isAir()) return originalState;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.STRUCTURE_SPAWNS)) return originalState;

        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(originalState.getBlock());
        if (blockKey == null) return originalState;

        ResourceLocation targetKey = mgr.getTable().lookup(
            mgr.getTable().getStructureSpawns(), blockKey, new java.util.Random());

        if (targetKey == null || targetKey.equals(blockKey)) return originalState;

        return BuiltInRegistries.BLOCK.getOptional(targetKey)
            .map(Block::defaultBlockState)
            .orElse(originalState);
    }
}

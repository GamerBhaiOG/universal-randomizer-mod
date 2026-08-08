package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Randomizes which block is actually placed when a player places a block.
 *
 * <p>Intercept strategy (Forge): {@code BlockEvent.EntityPlaceEvent} is cancelled
 * and the world is updated with the replacement block.
 * (Fabric): A Mixin on {@code BlockItem#place} or the equivalent Fabric callback.
 *
 * <p>The mapping is block RL → block RL (same properties set to default state).
 */
public final class BlockPlacementRandomizer {

    private BlockPlacementRandomizer() {}

    /**
     * Returns the {@link BlockState} that should actually be placed at the given
     * position, given what the player intended to place.
     */
    public static BlockState applyPlacement(BlockState intendedState) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.BLOCK_PLACEMENT)) return intendedState;

        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(intendedState.getBlock());
        if (blockKey == null) return intendedState;

        ResourceLocation targetKey = mgr.getBlockPlacement(blockKey);
        if (targetKey.equals(blockKey)) return intendedState;

        Block targetBlock = BuiltInRegistries.BLOCK.get(targetKey);
        RandomizerLogger.debug("BlockPlacement: {} -> {}", blockKey, targetKey);
        return targetBlock.defaultBlockState();
    }
}

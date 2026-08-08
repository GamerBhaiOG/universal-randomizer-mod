package com.universalrandomizer.forge;

import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.*;
import com.universalrandomizer.config.RandomizerMode;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handles Forge-specific block placement event routing.
 * Entity spawning, drops, smelting, loot tables, and fishing are handled centrally by common mixins.
 */
public class ForgeEventHandler {

    // ── Block Placement ────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || event.getLevel().isClientSide()) return;
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.BLOCK_PLACEMENT)) return;

        BlockState intended = event.getPlacedBlock();
        BlockState randomized = BlockPlacementRandomizer.applyPlacement(intended);
        if (!randomized.equals(intended)) {
            event.getLevel().setBlock(event.getPos(), randomized, 3);
        }
    }
}

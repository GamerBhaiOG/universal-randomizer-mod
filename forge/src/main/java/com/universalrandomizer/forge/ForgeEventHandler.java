package com.universalrandomizer.forge;

import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.*;
import com.universalrandomizer.config.RandomizerMode;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handles Forge-specific event routing (block placement, death drops).
 * Entity spawning, mob drops, smelting, loot tables, and fishing are handled centrally by common mixins / GLM.
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

    // ── Death Drops ────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player)) return;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.DEATH_DROPS)) return;

        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            ItemStack randomized = DeathDropRandomizer.randomizeDeathDrop(stack);
            if (!randomized.equals(stack)) {
                drop.setItem(randomized);
            }
        }
    }
}



package com.universalrandomizer.forge;

import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.*;
import com.universalrandomizer.config.RandomizerMode;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handles all Forge-specific events and routes them through the feature layer.
 *
 * <p>Events handled here:
 * <ul>
 *   <li>{@link BlockEvent.EntityPlaceEvent} → Block placement randomization</li>
 *   <li>{@link LivingDropsEvent} → Mob drop randomization (post-loot, fallback)</li>
 *   <li>{@link MobSpawnEvent.FinalizeSpawn} → Entity spawn randomization</li>
 *   <li>{@link VillagerTradesEvent} → Villager trade randomization</li>
 * </ul>
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

    // ── Entity Spawns ──────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onEntitySpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel().isClientSide()) return;
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.ENTITY_SPAWNS)) return;

        EntityType<?> intended = event.getEntity().getType();
        EntityType<?> randomized = EntitySpawnRandomizer.applySpawn(intended);
        if (!randomized.equals(intended)) {
            var level = event.getLevel();
            var replacement = randomized.create(level.getLevel());
            if (replacement != null) {
                replacement.setPos(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ());
                level.getLevel().addFreshEntity(replacement);
            }
            event.getEntity().discard();
            event.setCanceled(true);
        }
    }
}

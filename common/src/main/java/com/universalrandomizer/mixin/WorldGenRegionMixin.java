package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.WorldGenRandomizer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Intercepts feature and block placement during chunk world generation.
 */
@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {

    @ModifyVariable(
        method = "setBlock",
        at = @At("HEAD"),
        ordinal = 0,
        remap = true,
        require = 0
    )
    private BlockState universalRandomizer$randomizeWorldGenBlock(BlockState state) {
        if (state == null || state.isAir()) return state;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (mgr.isInitialized() && mgr.isEnabled(RandomizerMode.WORLD_GEN)) {
            return WorldGenRandomizer.randomizePlacedBlock(state);
        }
        return state;
    }
}

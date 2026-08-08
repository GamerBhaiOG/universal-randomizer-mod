package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.CropDropRandomizer;
import com.universalrandomizer.features.MiningDropRandomizer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Universal Mixin on {@link Block} to intercept item drops when blocks or crops are broken
 * on Fabric and Forge without infinite recursion or AIR block issues.
 */
@Mixin(Block.class)
public class BlockDropMixin {

    @Unique
    private static final ThreadLocal<Boolean> IS_POPPING = ThreadLocal.withInitial(() -> false);

    @Inject(
        method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = true,
        require = 0
    )
    private static void universalRandomizer$onPopResource(Level level, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        if (IS_POPPING.get() || level.isClientSide() || stack == null || stack.isEmpty()) return;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized()) return;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        ItemStack randomized = stack;

        // Check Crop Drops first
        if (mgr.isEnabled(RandomizerMode.CROP_DROPS) && CropDropRandomizer.isCropBlock(block, stack)) {
            randomized = CropDropRandomizer.applyDrop(block, stack);
        }
        // Otherwise check Mining Drops
        else if (mgr.isEnabled(RandomizerMode.MINING_DROPS)) {
            randomized = MiningDropRandomizer.applyDrop(stack.getItem(), stack);
        }

        if (!randomized.equals(stack)) {
            ci.cancel();
            try {
                IS_POPPING.set(true);
                Block.popResource(level, pos, randomized);
            } finally {
                IS_POPPING.set(false);
            }
        }
    }
}

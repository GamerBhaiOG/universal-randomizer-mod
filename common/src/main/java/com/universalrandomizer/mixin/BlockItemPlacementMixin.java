package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.BlockPlacementRandomizer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Universal Mixin on {@link BlockItem} to intercept player block placement.
 */
@Mixin(BlockItem.class)
public class BlockItemPlacementMixin {

    @Inject(
        method = "place",
        at = @At("HEAD"),
        cancellable = true,
        remap = true,
        require = 0
    )
    private void universalRandomizer$onPlaceBlock(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel().isClientSide()) return;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.BLOCK_PLACEMENT)) return;

        BlockItem item = (BlockItem)(Object)this;
        BlockState intendedState = item.getBlock().defaultBlockState();
        BlockState randomizedState = BlockPlacementRandomizer.applyPlacement(intendedState);

        if (!randomizedState.equals(intendedState)) {
            boolean placed = context.getLevel().setBlock(context.getClickedPos(), randomizedState, 3);
            if (placed) {
                if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
                cir.setReturnValue(InteractionResult.sidedSuccess(false));
            }
        }
    }
}

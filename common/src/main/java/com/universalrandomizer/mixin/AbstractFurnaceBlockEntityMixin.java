package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.SmeltingRandomizer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin on {@link AbstractFurnaceBlockEntity} to intercept furnace smelting outputs.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    /**
     * Intercepts item placement in the furnace output slot (slot index 2)
     * and modifies the parameter stack to be the randomized smelting result.
     */
    @ModifyVariable(
        method = "setItem",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        remap = true,
        require = 0
    )
    private ItemStack universalRandomizer$randomizeFurnaceOutput(ItemStack stack, int slot) {
        if (slot == 2 && stack != null && !stack.isEmpty()) {
            RandomizerManager mgr = RandomizerManager.getInstance();
            if (mgr.isInitialized() && mgr.isEnabled(RandomizerMode.SMELTING)) {
                return SmeltingRandomizer.randomizeSmeltingResult(stack);
            }
        }
        return stack;
    }
}

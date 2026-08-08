package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.SmeltingRandomizer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts furnace smelting output generation for continuous bulk smelting.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    @Inject(
        method = "burn",
        at = @At("HEAD"),
        cancellable = true,
        remap = true,
        require = 0
    )
    private void universalRandomizer$onBurn(RegistryAccess registryAccess, Recipe<?> recipe, NonNullList<ItemStack> items, int maxStackSize, CallbackInfoReturnable<Boolean> cir) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.SMELTING)) return;

        if (recipe != null && items != null && items.size() > 2) {
            ItemStack input = items.get(0);
            if (!input.isEmpty()) {
                ItemStack vanillaOutput = recipe.getResultItem(registryAccess);
                if (!vanillaOutput.isEmpty()) {
                    ItemStack randomizedOutput = SmeltingRandomizer.randomizeSmeltingResult(vanillaOutput);
                    ItemStack currentResultSlot = items.get(2);

                    if (currentResultSlot.isEmpty()) {
                        items.set(2, randomizedOutput.copy());
                    } else if (currentResultSlot.is(randomizedOutput.getItem())) {
                        currentResultSlot.grow(1);
                    } else {
                        ItemStack replacement = new ItemStack(randomizedOutput.getItem(), currentResultSlot.getCount() + 1);
                        items.set(2, replacement);
                    }
                    input.shrink(1);
                    cir.setReturnValue(true);
                }
            }
        }
    }

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

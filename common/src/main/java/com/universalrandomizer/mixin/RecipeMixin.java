package com.universalrandomizer.mixin;

import com.universalrandomizer.features.CraftingRandomizer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin on {@link Recipe#assemble} to intercept the crafting output and apply
 * the randomized result via {@link CraftingRandomizer}.
 *
 * <p>Targets the {@code assemble} method which is called when the recipe result
 * is actually requested (player takes item from output slot).
 *
 * <p>Compatible with all recipe types since the interface is universal.
 */
@Mixin(Recipe.class)
public interface RecipeMixin {

    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true, remap = true, require = 0)
    private <C extends Container> void universalRandomizer$onAssemble(
            C container,
            net.minecraft.core.RegistryAccess registryAccess,
            CallbackInfoReturnable<ItemStack> cir) {
        ItemStack original = cir.getReturnValue();
        if (original.isEmpty()) return;

        ItemStack randomized = CraftingRandomizer.applyOutput((Recipe<?>) this, original);
        if (!randomized.equals(original)) {
            cir.setReturnValue(randomized);
        }
    }
}

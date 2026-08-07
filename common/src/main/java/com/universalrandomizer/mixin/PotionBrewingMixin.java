package com.universalrandomizer.mixin;

import com.universalrandomizer.features.PotionBrewingRandomizer;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin on {@link PotionBrewing} to intercept potion result resolution.
 *
 * <p>In Minecraft 1.20.1 (Mojang mappings), the brew result is computed by the
 * static method {@code mix(ItemStack ingredient, ItemStack input)}. We inject at
 * RETURN to swap the resulting potion type through the randomizer mapping table.
 *
 * <p>The descriptor {@code (Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;}
 * matches the Mojang-mapped method signature for 1.20.1.
 */
@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {

    /**
     * Intercept mix result and apply randomization.
     * Both parameters are ItemStack in 1.20.1 Mojang mappings.
     */
    @Inject(
        method = "mix(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("RETURN"), cancellable = true, remap = true, require = 0
    )
    private static void universalRandomizer$onMix(
            ItemStack ingredient,
            ItemStack input,
            CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result == null || result.isEmpty()) return;

        // Extract current potion from result
        Potion currentPotion = net.minecraft.world.item.alchemy.PotionUtils.getPotion(result);
        Potion randomizedPotion = PotionBrewingRandomizer.applyPotion(currentPotion);
        if (!randomizedPotion.equals(currentPotion)) {
            ItemStack newResult = result.copy();
            net.minecraft.world.item.alchemy.PotionUtils.setPotion(newResult, randomizedPotion);
            cir.setReturnValue(newResult);
        }
    }
}

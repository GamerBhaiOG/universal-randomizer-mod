package com.universalrandomizer.mixin;

import com.universalrandomizer.features.CraftingRandomizer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin on {@link ResultContainer} to intercept all crafting recipe outputs.
 *
 * <p>In Minecraft, all crafting results (Crafting Table 3x3, Inventory 2x2,
 * Stonecutter, Smithing Table, etc.) are placed into a {@code ResultContainer}
 * via {@code setItem(0, stack)}. Intercepting here guarantees that the player
 * sees and receives the randomized item in the crafting output slot.
 */
@Mixin(ResultContainer.class)
public class ResultContainerMixin {

    @ModifyVariable(
        method = "setItem",
        at = @At("HEAD"),
        argsOnly = true,
        remap = true,
        require = 0
    )
    private ItemStack universalRandomizer$randomizeCraftingOutput(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;
        return CraftingRandomizer.randomizeStack(stack);
    }
}

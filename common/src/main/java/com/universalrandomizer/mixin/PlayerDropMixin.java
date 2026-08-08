package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.DeathDropRandomizer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin on Player to randomize item drops when a player dies.
 */
@Mixin(Player.class)
public class PlayerDropMixin {

    @ModifyVariable(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        remap = true,
        require = 0
    )
    private ItemStack universalRandomizer$randomizePlayerDeathDrop(ItemStack stack) {
        Player player = (Player) (Object) this;
        if (player.isDeadOrDying() && stack != null && !stack.isEmpty()) {
            RandomizerManager mgr = RandomizerManager.getInstance();
            if (mgr.isInitialized() && mgr.isEnabled(RandomizerMode.DEATH_DROPS)) {
                return DeathDropRandomizer.randomizeDeathDrop(stack);
            }
        }
        return stack;
    }
}

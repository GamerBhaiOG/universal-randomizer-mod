package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.ChestLootRandomizer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into LootTable to randomize all chest and structure container loot on Fabric and Forge.
 */
@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(
        method = "fill",
        at = @At("RETURN"),
        remap = true,
        require = 0
    )
    private void universalRandomizer$onFillContainer(Container container, LootParams params, long seed, CallbackInfo ci) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.CHEST_LOOT)) return;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                ItemStack randomized = ChestLootRandomizer.randomizeChestItem(stack);
                if (!randomized.equals(stack)) {
                    container.setItem(i, randomized);
                }
            }
        }
    }
}

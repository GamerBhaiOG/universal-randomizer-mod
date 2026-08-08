package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.FishingRandomizer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into FishingHook to randomize caught fishing loot items on Fabric and Forge.
 */
@Mixin(FishingHook.class)
public class FishingHookMixin {

    @Inject(
        method = "retrieve",
        at = @At("RETURN"),
        remap = true,
        require = 0
    )
    private void universalRandomizer$onRetrieve(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.FISHING_LOOT)) return;

        FishingHook hook = (FishingHook) (Object) this;
        hook.level().getEntitiesOfClass(ItemEntity.class, hook.getBoundingBox().inflate(4.0D)).forEach(entity -> {
            ItemStack original = entity.getItem();
            if (!original.isEmpty()) {
                ItemStack randomized = FishingRandomizer.randomizeFishingItem(original);
                if (!randomized.equals(original)) {
                    entity.setItem(randomized);
                }
            }
        });
    }
}
